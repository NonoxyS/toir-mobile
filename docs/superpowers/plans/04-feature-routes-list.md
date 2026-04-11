# Waypoint 04 — Feature Routes List (Список маршрутов)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать главный экран «Назначенные маршруты» — список RouteAssignment с прогрессом, кнопками «Начать» / «Продолжить».

**Architecture:** Данные читаются из локального SQLDelight. При нажатии «Начать» создаётся запись `Inspection` в локальной БД со статусом `IN_PROGRESS`. Переход на экран точек маршрута передаёт `inspectionId`.

**Tech Stack:** MVIKotlin, SQLDelight (RouteDao, InspectionDao), Compose Multiplatform.

---

## Файловая структура

```
shared/feature-routes-list/
├── api/src/commonMain/.../feature/routes/list/api/
│   ├── store/RoutesListStore.kt
│   └── models/DomainRouteAssignment.kt
├── impl/src/commonMain/.../feature/routes/list/impl/
│   ├── di/FeatureRoutesListImplModule.kt
│   ├── domain/repository/RoutesListRepository.kt        ← интерфейс в domain
│   ├── data/repository/RoutesListRepositoryImpl.kt
│   ├── data/mappers/RouteAssignmentMapper.kt
│   └── domain/RoutesListStoreFactory.kt  (+ Executor + Reducer)
├── presentation/src/commonMain/.../feature/routes/list/presentation/
│   ├── di/FeatureRoutesListPresentationModule.kt
│   ├── models/UiRoutesListState.kt
│   ├── models/UiRoutesListLabel.kt
│   ├── models/UiRouteAssignment.kt
│   ├── mappers/UiRoutesListStateMapper.kt
│   ├── mappers/UiRoutesListLabelMapper.kt
│   └── RoutesListViewModel.kt
└── ui/src/commonMain/.../feature/routes/list/ui/
    ├── api/FeatureRoutesListScreenApi.kt
    ├── RoutesListScreen.kt
    └── RouteAssignmentCard.kt
```

---

### Task 1: Регистрация и build-файлы

- [ ] **Step 1: Зарегистрировать модули в settings.gradle.kts**

```kotlin
include(":shared:feature-routes-list:api")
include(":shared:feature-routes-list:impl")
include(":shared:feature-routes-list:presentation")
include(":shared:feature-routes-list:ui")
```

- [ ] **Step 2: Создать директории**

```bash
mkdir -p shared/feature-routes-list/api/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/api/store
mkdir -p shared/feature-routes-list/api/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/api/models
mkdir -p shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/di
mkdir -p shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/data/repository
mkdir -p shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/data/mappers
mkdir -p shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/domain
mkdir -p shared/feature-routes-list/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/presentation/di
mkdir -p shared/feature-routes-list/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/presentation/models
mkdir -p shared/feature-routes-list/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/presentation/mappers
mkdir -p shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/api
```

- [ ] **Step 3: Создать build.gradle.kts для каждого субмодуля**

`api/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}
androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.routes.list.api"
}
```

`impl/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}
androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.routes.list.impl"
}
commonMainDependencies {
    implementations(projects.shared.coreDatabase)
}
```

`presentation/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.routes.list.presentation"
}
```

`ui/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.routes.list.ui"
}
```

---

### Task 2: Domain-модели и Store (api)

**Files:**
- Create: `.../feature/routes/list/api/models/DomainRouteAssignment.kt`
- Create: `.../feature/routes/list/api/store/RoutesListStore.kt`

- [ ] **Step 1: DomainRouteAssignment**

```kotlin
package ru.mirea.toir.feature.routes.list.api.models

data class DomainRouteAssignment(
    val assignmentId: String,
    val routeId: String,
    val routeName: String,
    val status: String,
    val assignedAt: String,
    val dueDate: String?,
    val totalPoints: Int,
    val completedPoints: Int,
    val inspectionId: String?,
)
```

- [ ] **Step 2: RoutesListStore**

```kotlin
package ru.mirea.toir.feature.routes.list.api.store

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

interface RoutesListStore : Store<RoutesListStore.Intent, RoutesListStore.State, RoutesListStore.Label> {

    data class State(
        val assignments: ImmutableList<DomainRouteAssignment> = persistentListOf(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object Refresh : Intent
        data class OnStartInspection(val assignmentId: String) : Intent
        data class OnContinueInspection(val inspectionId: String) : Intent
    }

    sealed interface Label {
        data class NavigateToRoutePoints(val inspectionId: String) : Label
    }
}
```

---

### Task 3: Repository (impl)

**Files:**
- Create: `.../feature/routes/list/impl/domain/repository/RoutesListRepository.kt`
- Create: `.../feature/routes/list/impl/data/mappers/RouteAssignmentMapper.kt`
- Create: `.../feature/routes/list/impl/data/repository/RoutesListRepositoryImpl.kt`

- [ ] **Step 1: RoutesListRepository**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.data.repository

import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

internal interface RoutesListRepository {
    suspend fun getAssignments(): Result<List<DomainRouteAssignment>>
    suspend fun startInspection(assignmentId: String): Result<String>
}
```

- [ ] **Step 2: RouteAssignmentMapper**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.data.mappers

import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Routes
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

internal class RouteAssignmentMapper {
    fun map(
        assignment: Route_assignments,
        route: Routes?,
        totalPoints: Int,
        completedPoints: Int,
        inspectionId: String?,
    ): DomainRouteAssignment = DomainRouteAssignment(
        assignmentId = assignment.id,
        routeId = assignment.route_id,
        routeName = route?.name.orEmpty(),
        status = assignment.status,
        assignedAt = assignment.assigned_at,
        dueDate = assignment.due_date,
        totalPoints = totalPoints,
        completedPoints = completedPoints,
        inspectionId = inspectionId,
    )
}
```

- [ ] **Step 3: RoutesListRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock

internal class RoutesListRepositoryImpl(
    private val routeDao: RouteDao,
    private val inspectionDao: InspectionDao,
    private val mapper: RouteAssignmentMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutesListRepository {

    override suspend fun getAssignments(): Result<List<DomainRouteAssignment>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val assignments = routeDao.selectAllAssignments()
                    val result = assignments.map { assignment ->
                        val route = routeDao.selectRouteById(assignment.route_id)
                        val points = routeDao.selectPointsByRouteId(assignment.route_id)
                        val inspection = inspectionDao.selectByAssignmentId(assignment.id)
                        val completed = if (inspection != null) {
                            inspectionDao.selectEquipmentResultsByInspectionId(inspection.id)
                                .count { it.status == "COMPLETED" }
                        } else 0
                        mapper.map(
                            assignment = assignment,
                            route = route,
                            totalPoints = points.size,
                            completedPoints = completed,
                            inspectionId = inspection?.id,
                        )
                    }
                    result.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getAssignments failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun startInspection(assignmentId: String): Result<String> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val existing = inspectionDao.selectByAssignmentId(assignmentId)
                    if (existing != null) return@coRunCatching existing.id.wrapResultSuccess()

                    val assignment = routeDao.selectAssignmentById(assignmentId)
                        ?: error("Assignment not found: $assignmentId")
                    val inspectionId = Uuid.random().toString()
                    inspectionDao.insertInspection(
                        id = inspectionId,
                        assignmentId = assignmentId,
                        routeId = assignment.route_id,
                        status = "IN_PROGRESS",
                        startedAt = Clock.System.now().toString(),
                    )
                    routeDao.updateAssignmentStatus(id = assignmentId, status = "IN_PROGRESS")
                    inspectionId.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "startInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

---

### Task 4: Executor, Reducer, StoreFactory (impl)

**Files:**
- Create: `.../feature/routes/list/impl/domain/RoutesListReducer.kt`
- Create: `.../feature/routes/list/impl/domain/RoutesListExecutor.kt`
- Create: `.../feature/routes/list/impl/domain/RoutesListStoreFactory.kt`
- Create: `impl/src/commonTest/kotlin/ru/mirea/toir/feature/routes/list/impl/domain/RoutesListReducerTest.kt`

- [ ] **Step 1: Написать тест для RoutesListReducer**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoutesListReducerTest {
    private val reducer = RoutesListReducer()
    private val initial = RoutesListStore.State()

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(RoutesListStoreFactory.Message.SetLoading) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetError sets errorMessage and clears loading`() {
        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(RoutesListStoreFactory.Message.SetError("Ошибка"))
        }
        assertFalse(result.isLoading)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `SetAssignments replaces list and clears loading`() {
        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(RoutesListStoreFactory.Message.SetAssignments(persistentListOf()))
        }
        assertFalse(result.isLoading)
        assertTrue(result.assignments.isEmpty())
    }
}
```

- [ ] **Step 2: Запустить тест — убедиться, что он падает**

```bash
./gradlew :shared:feature-routes-list:impl:testDebugUnitTest 2>&1 | tail -10
```

- [ ] **Step 3: Создать RoutesListReducer**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore

internal class RoutesListReducer : Reducer<RoutesListStore.State, RoutesListStoreFactory.Message> {
    override fun RoutesListStore.State.reduce(msg: RoutesListStoreFactory.Message): RoutesListStore.State = when (msg) {
        RoutesListStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is RoutesListStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        is RoutesListStoreFactory.Message.SetAssignments -> copy(isLoading = false, assignments = msg.list, errorMessage = null)
    }
}
```

- [ ] **Step 4: Запустить тест — убедиться, что он проходит**

```bash
./gradlew :shared:feature-routes-list:impl:testDebugUnitTest
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 5: Создать RoutesListExecutor**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.impl.data.repository.RoutesListRepository

internal class RoutesListExecutor(
    private val repository: RoutesListRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<RoutesListStore.Intent, Unit, RoutesListStore.State, RoutesListStoreFactory.Message, RoutesListStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        loadAssignments()
    }

    override suspend fun suspendExecuteIntent(intent: RoutesListStore.Intent) {
        when (intent) {
            RoutesListStore.Intent.Refresh -> loadAssignments()
            is RoutesListStore.Intent.OnStartInspection -> startInspection(intent.assignmentId)
            is RoutesListStore.Intent.OnContinueInspection -> publish(
                RoutesListStore.Label.NavigateToRoutePoints(intent.inspectionId)
            )
        }
    }

    private suspend fun loadAssignments() {
        dispatch(RoutesListStoreFactory.Message.SetLoading)
        repository.getAssignments().fold(
            onSuccess = { list ->
                dispatch(RoutesListStoreFactory.Message.SetAssignments(list.toImmutableList()))
            },
            onFailure = { throwable ->
                dispatch(RoutesListStoreFactory.Message.SetError(throwable.message ?: "Ошибка загрузки"))
            },
        )
    }

    private suspend fun startInspection(assignmentId: String) {
        repository.startInspection(assignmentId).fold(
            onSuccess = { inspectionId ->
                publish(RoutesListStore.Label.NavigateToRoutePoints(inspectionId))
            },
            onFailure = { throwable ->
                dispatch(RoutesListStoreFactory.Message.SetError(throwable.message ?: "Ошибка старта обхода"))
            },
        )
    }
}
```

- [ ] **Step 6: Создать RoutesListStoreFactory**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.impl.data.repository.RoutesListRepository

internal class RoutesListStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RoutesListRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): RoutesListStore =
        object :
            RoutesListStore,
            Store<RoutesListStore.Intent, RoutesListStore.State, RoutesListStore.Label> by storeFactory.create(
                name = RoutesListStore::class.simpleName,
                initialState = RoutesListStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { RoutesListExecutor(repository, mainDispatcher) },
                reducer = RoutesListReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data class SetAssignments(val list: ImmutableList<DomainRouteAssignment>) : Message
    }
}
```

- [ ] **Step 7: Создать DI-модуль**

```kotlin
package ru.mirea.toir.feature.routes.list.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.data.repository.RoutesListRepository
import ru.mirea.toir.feature.routes.list.impl.data.repository.RoutesListRepositoryImpl
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory

val featureRoutesListImplModule = module {
    factory { new(::RouteAssignmentMapper) }
    factory<RoutesListRepository> { new(::RoutesListRepositoryImpl) }
    factory { new(::RoutesListStoreFactory) }
}
```

---

### Task 5: Presentation-модуль

**Files:**
- Create: `.../feature/routes/list/presentation/models/UiRoutesListState.kt`
- Create: `.../feature/routes/list/presentation/models/UiRoutesListLabel.kt`
- Create: `.../feature/routes/list/presentation/models/UiRouteAssignment.kt`
- Create: `.../feature/routes/list/presentation/mappers/UiRoutesListStateMapper.kt`
- Create: `.../feature/routes/list/presentation/mappers/UiRoutesListLabelMapper.kt`
- Create: `.../feature/routes/list/presentation/RoutesListViewModel.kt`
- Create: `.../feature/routes/list/presentation/di/FeatureRoutesListPresentationModule.kt`

- [ ] **Step 1: Модели**

`UiRouteAssignment.kt`:
```kotlin
package ru.mirea.toir.feature.routes.list.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiRouteAssignment(
    val assignmentId: String,
    val routeName: String,
    val statusLabel: String,
    val progressText: String,
    val isAssigned: Boolean,
    val isInProgress: Boolean,
    val inspectionId: String?,
)
```

`UiRoutesListState.kt`:
```kotlin
package ru.mirea.toir.feature.routes.list.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiRoutesListState(
    val assignments: ImmutableList<UiRouteAssignment> = persistentListOf(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
```

`UiRoutesListLabel.kt`:
```kotlin
package ru.mirea.toir.feature.routes.list.presentation.models

sealed interface UiRoutesListLabel {
    data class NavigateToRoutePoints(val inspectionId: String) : UiRoutesListLabel
}
```

- [ ] **Step 2: StateMapper**

```kotlin
package ru.mirea.toir.feature.routes.list.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListState

interface UiRoutesListStateMapper {
    fun map(state: RoutesListStore.State): UiRoutesListState
}

internal class UiRoutesListStateMapperImpl : UiRoutesListStateMapper {
    override fun map(state: RoutesListStore.State): UiRoutesListState = with(state) {
        UiRoutesListState(
            assignments = assignments.map { it.toUi() }.toImmutableList(),
            isLoading = isLoading,
            errorMessage = errorMessage,
        )
    }

    private fun DomainRouteAssignment.toUi(): UiRouteAssignment = UiRouteAssignment(
        assignmentId = assignmentId,
        routeName = routeName,
        statusLabel = when (status) {
            "ASSIGNED" -> "Назначен"
            "IN_PROGRESS" -> "В процессе"
            "COMPLETED" -> "Завершён"
            else -> status
        },
        progressText = "$completedPoints из $totalPoints точек",
        isAssigned = status == "ASSIGNED",
        isInProgress = status == "IN_PROGRESS",
        inspectionId = inspectionId,
    )
}
```

- [ ] **Step 3: LabelMapper**

```kotlin
package ru.mirea.toir.feature.routes.list.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel

interface UiRoutesListLabelMapper : Mapper<RoutesListStore.Label, UiRoutesListLabel>

internal class UiRoutesListLabelMapperImpl : UiRoutesListLabelMapper {
    override fun map(item: RoutesListStore.Label): UiRoutesListLabel = when (item) {
        is RoutesListStore.Label.NavigateToRoutePoints ->
            UiRoutesListLabel.NavigateToRoutePoints(item.inspectionId)
    }
}
```

- [ ] **Step 4: RoutesListViewModel**

```kotlin
package ru.mirea.toir.feature.routes.list.presentation

import ru.mirea.toir.core.presentation.BaseViewModel
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapper
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListState

class RoutesListViewModel internal constructor(
    private val store: RoutesListStore,
    private val stateMapper: UiRoutesListStateMapper,
    private val labelMapper: UiRoutesListLabelMapper,
) : BaseViewModel<UiRoutesListState, UiRoutesListLabel>(initialState = UiRoutesListState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRefresh() = store.accept(RoutesListStore.Intent.Refresh)
    fun onStartInspection(assignmentId: String) = store.accept(RoutesListStore.Intent.OnStartInspection(assignmentId))
    fun onContinueInspection(inspectionId: String) = store.accept(RoutesListStore.Intent.OnContinueInspection(inspectionId))

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
```

- [ ] **Step 5: DI-модуль presentation**

```kotlin
package ru.mirea.toir.feature.routes.list.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.routes.list.presentation.RoutesListViewModel
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapperImpl
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapperImpl

val featureRoutesListPresentationModule = module {
    factory<UiRoutesListStateMapper> { new(::UiRoutesListStateMapperImpl) }
    factory<UiRoutesListLabelMapper> { new(::UiRoutesListLabelMapperImpl) }
    viewModelOf(::RoutesListViewModel)
}
```

---

### Task 6: UI-модуль

**Files:**
- Create: `.../feature/routes/list/ui/api/FeatureRoutesListScreenApi.kt`
- Create: `.../feature/routes/list/ui/RouteAssignmentCard.kt`
- Create: `.../feature/routes/list/ui/RoutesListScreen.kt`

Добавить строки в strings.xml:
```xml
<!-- routes_list -->
<string name="routes_list_title">Назначенные маршруты</string>
<string name="routes_list_empty">Нет назначенных маршрутов</string>
<string name="routes_list_button_start">Начать</string>
<string name="routes_list_button_continue">Продолжить</string>
<string name="routes_list_button_completed">Завершён</string>
```

- [ ] **Step 1: Добавить RoutePointsRoute в Screen.kt**

```kotlin
@Serializable
data class RoutePointsRoute(val inspectionId: String) : Screen
```

- [ ] **Step 2: FeatureRoutesListScreenApi**

```kotlin
package ru.mirea.toir.feature.routes.list.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.RoutePointsRoute
import ru.mirea.toir.core.navigation.RoutesListRoute
import ru.mirea.toir.feature.routes.list.ui.RoutesListScreen

fun NavGraphBuilder.composableRoutesListScreen(
    onNavigateToRoutePoints: (inspectionId: String) -> Unit,
) {
    composable<RoutesListRoute> {
        RoutesListScreen(onNavigateToRoutePoints = onNavigateToRoutePoints)
    }
}

fun NavController.navigateToRoutesList() {
    navigate(RoutesListRoute)
}
```

- [ ] **Step 3: RouteAssignmentCard**

```kotlin
package ru.mirea.toir.feature.routes.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.res.MR

@Composable
internal fun RouteAssignmentCard(
    item: UiRouteAssignment,
    onStartClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = item.routeName, style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(text = item.statusLabel, style = MaterialTheme.typography.bodySmall)
                    Text(text = item.progressText, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                when {
                    item.isAssigned -> Button(onClick = onStartClick) {
                        Text(text = stringResource(MR.strings.routes_list_button_start))
                    }
                    item.isInProgress -> Button(onClick = onContinueClick) {
                        Text(text = stringResource(MR.strings.routes_list_button_continue))
                    }
                    else -> Text(
                        text = stringResource(MR.strings.routes_list_button_completed),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRouteAssignmentCard() {
    ToirTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RouteAssignmentCard(
                item = UiRouteAssignment(
                    assignmentId = "1",
                    routeName = "Маршрут №1 — Корпус А",
                    statusLabel = "Назначен",
                    progressText = "0 из 5 точек",
                    isAssigned = true,
                    isInProgress = false,
                    inspectionId = null,
                ),
                onStartClick = {},
                onContinueClick = {},
            )
            RouteAssignmentCard(
                item = UiRouteAssignment(
                    assignmentId = "2",
                    routeName = "Маршрут №2 — Корпус Б",
                    statusLabel = "В процессе",
                    progressText = "3 из 8 точек",
                    isAssigned = false,
                    isInProgress = true,
                    inspectionId = "insp-2",
                ),
                onStartClick = {},
                onContinueClick = {},
            )
        }
    }
}
```

- [ ] **Step 4: RoutesListScreen**

```kotlin
package ru.mirea.toir.feature.routes.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.routes.list.presentation.RoutesListViewModel
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutesListScreen(
    onNavigateToRoutePoints: (inspectionId: String) -> Unit,
    viewModel: RoutesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiRoutesListLabel.NavigateToRoutePoints -> onNavigateToRoutePoints(label.inspectionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(MR.strings.routes_list_title)) })
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.assignments.isEmpty() -> Text(text = stringResource(MR.strings.routes_list_empty))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = state.assignments, key = { it.assignmentId }) { item ->
                        RouteAssignmentCard(
                            item = item,
                            onStartClick = { viewModel.onStartInspection(item.assignmentId) },
                            onContinueClick = { viewModel.onContinueInspection(item.inspectionId.orEmpty()) },
                        )
                    }
                }
            }
        }
    }
}
```

---

### Task 7: Подключить в App.kt и Koin

- [ ] **Step 1: Добавить модули в Koin.kt**

```kotlin
import ru.mirea.toir.feature.routes.list.impl.di.featureRoutesListImplModule
import ru.mirea.toir.feature.routes.list.presentation.di.featureRoutesListPresentationModule
// ...
featureRoutesListImplModule,
featureRoutesListPresentationModule,
```

- [ ] **Step 2: Подключить экран в App.kt**

В NavHost добавить после `composableBootstrapScreen`:
```kotlin
import ru.mirea.toir.core.navigation.RoutePointsRoute
import ru.mirea.toir.feature.routes.list.ui.api.composableRoutesListScreen

composableRoutesListScreen(
    onNavigateToRoutePoints = { inspectionId ->
        navController.navigate(RoutePointsRoute(inspectionId))
    },
)
```

- [ ] **Step 3: Собрать и проверить**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 4: Ручная проверка**
  1. Пройти авторизацию → bootstrap
  2. Должен появиться экран «Назначенные маршруты»
  3. Карточки маршрутов из локальной БД отображаются
  4. Кнопки «Начать» / «Продолжить» видны согласно статусу
  5. Нажатие «Начать» создаёт `Inspection` и переходит на `RoutePointsRoute`

- [ ] **Step 5: Commit**

```bash
git add shared/feature-routes-list/ settings.gradle.kts shared/main/ shared/core-navigation/ shared/common-resources/
git commit -m "feat: implement routes list screen with start/continue inspection support"
```
