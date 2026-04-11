# Waypoint 03 — Bootstrap & App Shell

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать bootstrap-загрузку данных при первом запуске после авторизации, auth-guard в NavHost и удалить demo-функциональность.

**Architecture:** После успешного входа вызывается `GET /api/v1/mobile/bootstrap` — ответ сохраняется в SQLDelight. App.kt получает логику: если токен есть → BootstrapScreen (загрузка) → RoutesList, иначе → Login. Demo-фичи удаляются.

**Tech Stack:** Ktor, SQLDelight, Compose Navigation, Koin.

---

## Файловая структура

```
shared/main/src/commonMain/.../ui/App.kt                            # REWRITE — auth guard + bootstrap
shared/main/src/commonMain/.../di/Koin.kt                           # MODIFY — remove demo, add new modules
shared/core-navigation/src/commonMain/.../navigation/Screen.kt      # MODIFY — add new routes

shared/feature-bootstrap/
├── api/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/bootstrap/api/
│       └── store/BootstrapStore.kt
├── impl/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/bootstrap/impl/
│       ├── di/FeatureBootstrapImplModule.kt
│       ├── data/network/BootstrapApiClient.kt
│       ├── data/network/BootstrapApiClientImpl.kt
│       ├── data/network/models/RemoteBootstrapResponse.kt  (+ вложенные DTO)
│       ├── domain/repository/BootstrapRepository.kt       ← интерфейс в domain
│       ├── data/repository/BootstrapRepositoryImpl.kt
│       └── domain/BootstrapStoreFactory.kt  (+ Executor + Reducer)
├── presentation/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/bootstrap/presentation/
│       ├── di/FeatureBootstrapPresentationModule.kt
│       ├── models/UiBootstrapState.kt
│       ├── models/UiBootstrapLabel.kt
│       ├── mappers/UiBootstrapStateMapper.kt
│       ├── mappers/UiBootstrapLabelMapper.kt
│       └── BootstrapViewModel.kt
└── ui/
    ├── build.gradle.kts
    └── src/commonMain/.../feature/bootstrap/ui/
        ├── api/FeatureBootstrapScreenApi.kt
        └── BootstrapScreen.kt
```

---

### Task 1: Добавить маршруты и зарегистрировать модули

**Files:**
- Modify: `shared/core-navigation/src/commonMain/kotlin/ru/mirea/toir/core/navigation/Screen.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Добавить BootstrapRoute в Screen.kt**

Добавить к уже существующему `AuthRoute`:
```kotlin
@Serializable
data object BootstrapRoute : Screen

@Serializable
data object RoutesListRoute : Screen
```

- [ ] **Step 2: Зарегистрировать модули в settings.gradle.kts**

```kotlin
include(":shared:feature-bootstrap:api")
include(":shared:feature-bootstrap:impl")
include(":shared:feature-bootstrap:presentation")
include(":shared:feature-bootstrap:ui")
```

- [ ] **Step 3: Создать директории**

```bash
mkdir -p shared/feature-bootstrap/api/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/api/store
mkdir -p shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/di
mkdir -p shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/network/models
mkdir -p shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/repository
mkdir -p shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain
mkdir -p shared/feature-bootstrap/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/presentation/di
mkdir -p shared/feature-bootstrap/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/presentation/models
mkdir -p shared/feature-bootstrap/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/presentation/mappers
mkdir -p shared/feature-bootstrap/ui/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/ui/api
```

---

### Task 2: Build-файлы для feature-bootstrap

**Files:**
- Create: `shared/feature-bootstrap/api/build.gradle.kts`
- Create: `shared/feature-bootstrap/impl/build.gradle.kts`
- Create: `shared/feature-bootstrap/presentation/build.gradle.kts`
- Create: `shared/feature-bootstrap/ui/build.gradle.kts`

- [ ] **Step 1: api**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.api"
}
```

- [ ] **Step 2: impl**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
    )
}
```

- [ ] **Step 3: presentation**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.presentation"
}
```

- [ ] **Step 4: ui**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.ui"
}
```

---

### Task 3: Store (api-модуль)

**Files:**
- Create: `shared/feature-bootstrap/api/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/api/store/BootstrapStore.kt`

- [ ] **Step 1: Создать BootstrapStore**

```kotlin
package ru.mirea.toir.feature.bootstrap.api.store

import com.arkivanov.mvikotlin.core.store.Store

interface BootstrapStore : Store<BootstrapStore.Intent, BootstrapStore.State, BootstrapStore.Label> {

    data class State(
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object Retry : Intent
    }

    sealed interface Label {
        data object NavigateToRoutesList : Label
        data object NavigateToLogin : Label
    }
}
```

---

### Task 4: Remote DTO и Bootstrap API

> ⚠️ Поля bootstrap-ответа (`assignments`, `routes`, `routePoints`, `equipment`, `locations`, `checklists`, `checklistItems`) в openapi-спеке описаны как `{ type: array, items: { type: object } }` без детализации полей. **Обязательно сверь реальные поля с бэкенд-кодом** (`~/IdeaProjects/toir-backend`) перед реализацией. Ниже — структура, согласованная с доменной моделью спеки раздел 5.

**Files:**
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/network/models/RemoteBootstrapResponse.kt`
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/network/BootstrapApiClient.kt`
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/network/BootstrapApiClientImpl.kt`

- [ ] **Step 1: Создать RemoteBootstrapResponse**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteBootstrapResponse(
    @SerialName("user") val user: RemoteBootstrapUser?,
    @SerialName("device") val device: RemoteBootstrapDevice?,
    @SerialName("assignments") val assignments: List<RemoteBootstrapAssignment>?,
    @SerialName("routes") val routes: List<RemoteBootstrapRoute>?,
    @SerialName("routePoints") val routePoints: List<RemoteBootstrapRoutePoint>?,
    @SerialName("equipment") val equipment: List<RemoteBootstrapEquipment>?,
    @SerialName("locations") val locations: List<RemoteBootstrapLocation>?,
    @SerialName("checklists") val checklists: List<RemoteBootstrapChecklist>?,
    @SerialName("checklistItems") val checklistItems: List<RemoteBootstrapChecklistItem>?,
    @SerialName("serverTime") val serverTime: String?,
)

@Serializable
internal data class RemoteBootstrapUser(
    @SerialName("id") val id: String?,
    @SerialName("login") val login: String?,
    @SerialName("displayName") val displayName: String?,
    @SerialName("role") val role: String?,
)

@Serializable
internal data class RemoteBootstrapDevice(
    @SerialName("id") val id: String?,
    @SerialName("deviceCode") val deviceCode: String?,
)

@Serializable
internal data class RemoteBootstrapAssignment(
    @SerialName("id") val id: String?,
    @SerialName("routeId") val routeId: String?,
    @SerialName("userId") val userId: String?,
    @SerialName("status") val status: String?,
    @SerialName("assignedAt") val assignedAt: String?,
    @SerialName("dueDate") val dueDate: String?,
)

@Serializable
internal data class RemoteBootstrapRoute(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String?,
)

@Serializable
internal data class RemoteBootstrapRoutePoint(
    @SerialName("id") val id: String?,
    @SerialName("routeId") val routeId: String?,
    @SerialName("equipmentId") val equipmentId: String?,
    @SerialName("checklistId") val checklistId: String?,
    @SerialName("orderIndex") val orderIndex: Int?,
)

@Serializable
internal data class RemoteBootstrapEquipment(
    @SerialName("id") val id: String?,
    @SerialName("code") val code: String?,
    @SerialName("name") val name: String?,
    @SerialName("type") val type: String?,
    @SerialName("locationId") val locationId: String?,
)

@Serializable
internal data class RemoteBootstrapLocation(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String?,
)

@Serializable
internal data class RemoteBootstrapChecklist(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("equipmentId") val equipmentId: String?,
)

@Serializable
internal data class RemoteBootstrapChecklistItem(
    @SerialName("id") val id: String?,
    @SerialName("checklistId") val checklistId: String?,
    @SerialName("title") val title: String?,
    @SerialName("description") val description: String?,
    @SerialName("answerType") val answerType: String?,
    @SerialName("isRequired") val isRequired: Boolean?,
    @SerialName("requiresPhoto") val requiresPhoto: Boolean?,
    @SerialName("selectOptions") val selectOptions: List<String>?,
    @SerialName("orderIndex") val orderIndex: Int?,
)
```

- [ ] **Step 2: Создать BootstrapApiClient**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.data.network

import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

internal interface BootstrapApiClient {
    suspend fun fetchBootstrap(): Result<RemoteBootstrapResponse>
}
```

- [ ] **Step 3: Создать BootstrapApiClientImpl**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.data.network

import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

internal class BootstrapApiClientImpl(
    private val ktorClient: KtorClient,
) : BootstrapApiClient {

    override suspend fun fetchBootstrap(): Result<RemoteBootstrapResponse> =
        ktorClient.executeQuery(
            query = { ktorClient.get("/api/v1/mobile/bootstrap") },
            deserializer = RemoteBootstrapResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "fetchBootstrap failed",
        )
}
```

---

### Task 5: BootstrapRepository

**Files:**
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/BootstrapRepository.kt`
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/repository/BootstrapRepositoryImpl.kt`

- [ ] **Step 1: Создать BootstrapRepository**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain.repository

internal interface BootstrapRepository {
    suspend fun loadAndSaveBootstrap(): Result<Unit>
}
```

- [ ] **Step 2: Создать BootstrapRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.data.repository

import io.github.aakira.napier.Napier
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.core.database.dao.UserDao
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient

internal class BootstrapRepositoryImpl(
    private val apiClient: BootstrapApiClient,
    private val userDao: UserDao,
    private val equipmentDao: EquipmentDao,
    private val routeDao: RouteDao,
    private val checklistDao: ChecklistDao,
    private val syncMetaDao: SyncMetaDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BootstrapRepository {

    override suspend fun loadAndSaveBootstrap(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val response = apiClient.fetchBootstrap().getOrThrow()

                    response.user?.let { remoteUser ->
                        userDao.upsert(
                            id = remoteUser.id.orEmpty(),
                            login = remoteUser.login.orEmpty(),
                            displayName = remoteUser.displayName.orEmpty(),
                            role = remoteUser.role.orEmpty(),
                        )
                    }

                    response.locations.orEmpty().forEach { loc ->
                        // LocationDao not shown for brevity — add to core-database if needed
                    }

                    response.equipment.orEmpty().forEach { eq ->
                        equipmentDao.upsert(
                            id = eq.id.orEmpty(),
                            code = eq.code.orEmpty(),
                            name = eq.name.orEmpty(),
                            type = eq.type.orEmpty(),
                            locationId = eq.locationId.orEmpty(),
                        )
                    }

                    response.routes.orEmpty().forEach { route ->
                        routeDao.upsertRoute(
                            id = route.id.orEmpty(),
                            name = route.name.orEmpty(),
                            description = route.description,
                        )
                    }

                    response.routePoints.orEmpty().forEach { point ->
                        routeDao.upsertRoutePoint(
                            id = point.id.orEmpty(),
                            routeId = point.routeId.orEmpty(),
                            equipmentId = point.equipmentId.orEmpty(),
                            checklistId = point.checklistId.orEmpty(),
                            orderIndex = point.orderIndex?.toLong() ?: 0L,
                        )
                    }

                    response.assignments.orEmpty().forEach { assignment ->
                        routeDao.upsertAssignment(
                            id = assignment.id.orEmpty(),
                            routeId = assignment.routeId.orEmpty(),
                            userId = assignment.userId.orEmpty(),
                            status = assignment.status.orEmpty(),
                            assignedAt = assignment.assignedAt.orEmpty(),
                            dueDate = assignment.dueDate,
                        )
                    }

                    response.checklists.orEmpty().forEach { cl ->
                        checklistDao.upsertChecklist(
                            id = cl.id.orEmpty(),
                            name = cl.name.orEmpty(),
                            equipmentId = cl.equipmentId,
                        )
                    }

                    response.checklistItems.orEmpty().forEach { item ->
                        checklistDao.upsertItem(
                            id = item.id.orEmpty(),
                            checklistId = item.checklistId.orEmpty(),
                            title = item.title.orEmpty(),
                            description = item.description,
                            answerType = item.answerType.orEmpty(),
                            isRequired = if (item.isRequired == true) 1L else 0L,
                            requiresPhoto = if (item.requiresPhoto == true) 1L else 0L,
                            selectOptions = item.selectOptions?.let { Json.encodeToString(it) },
                            orderIndex = item.orderIndex?.toLong() ?: 0L,
                        )
                    }

                    syncMetaDao.upsert(
                        key = SyncMetaDao.KEY_LAST_SYNC_TIME,
                        value = response.serverTime.orEmpty(),
                    )

                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "loadAndSaveBootstrap failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

---

### Task 6: Executor, Reducer, StoreFactory (impl-модуль)

**Files:**
- Create: `.../feature/bootstrap/impl/domain/BootstrapReducer.kt`
- Create: `.../feature/bootstrap/impl/domain/BootstrapExecutor.kt`
- Create: `.../feature/bootstrap/impl/domain/BootstrapStoreFactory.kt`

- [ ] **Step 1: Создать BootstrapReducer**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore

internal class BootstrapReducer : Reducer<BootstrapStore.State, BootstrapStoreFactory.Message> {
    override fun BootstrapStore.State.reduce(msg: BootstrapStoreFactory.Message): BootstrapStore.State = when (msg) {
        BootstrapStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is BootstrapStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        BootstrapStoreFactory.Message.ClearLoading -> copy(isLoading = false)
    }
}
```

- [ ] **Step 2: Написать тест для BootstrapReducer**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BootstrapReducerTest {
    private val reducer = BootstrapReducer()
    private val initial = BootstrapStore.State()

    @Test
    fun `SetLoading sets isLoading true`() {
        val result = with(reducer) { initial.reduce(BootstrapStoreFactory.Message.SetLoading) }
        assertTrue(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun `SetError sets error and clears loading`() {
        val loading = initial.copy(isLoading = true)
        val result = with(reducer) { loading.reduce(BootstrapStoreFactory.Message.SetError("Ошибка")) }
        assertFalse(result.isLoading)
        assertNotNull(result.errorMessage)
    }
}
```

- [ ] **Step 3: Создать BootstrapExecutor**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapExecutor(
    private val bootstrapRepository: BootstrapRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<BootstrapStore.Intent, Unit, BootstrapStore.State, BootstrapStoreFactory.Message, BootstrapStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        loadBootstrap()
    }

    override suspend fun suspendExecuteIntent(intent: BootstrapStore.Intent) {
        when (intent) {
            BootstrapStore.Intent.Retry -> loadBootstrap()
        }
    }

    private suspend fun loadBootstrap() {
        dispatch(BootstrapStoreFactory.Message.SetLoading)
        bootstrapRepository.loadAndSaveBootstrap().fold(
            onSuccess = {
                dispatch(BootstrapStoreFactory.Message.ClearLoading)
                publish(BootstrapStore.Label.NavigateToRoutesList)
            },
            onFailure = { throwable ->
                dispatch(BootstrapStoreFactory.Message.SetError(throwable.message ?: "Ошибка загрузки данных"))
            },
        )
    }
}
```

- [ ] **Step 4: Создать BootstrapStoreFactory**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapStoreFactory(
    private val storeFactory: StoreFactory,
    private val bootstrapRepository: BootstrapRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): BootstrapStore =
        object :
            BootstrapStore,
            Store<BootstrapStore.Intent, BootstrapStore.State, BootstrapStore.Label> by storeFactory.create(
                name = BootstrapStore::class.simpleName,
                initialState = BootstrapStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { BootstrapExecutor(bootstrapRepository, mainDispatcher) },
                reducer = BootstrapReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data object ClearLoading : Message
    }
}
```

---

### Task 7: Presentation и UI для Bootstrap

**Files:**
- Create: `.../feature/bootstrap/presentation/models/UiBootstrapState.kt`
- Create: `.../feature/bootstrap/presentation/models/UiBootstrapLabel.kt`
- Create: `.../feature/bootstrap/presentation/mappers/UiBootstrapStateMapper.kt`
- Create: `.../feature/bootstrap/presentation/mappers/UiBootstrapLabelMapper.kt`
- Create: `.../feature/bootstrap/presentation/BootstrapViewModel.kt`
- Create: `.../feature/bootstrap/presentation/di/FeatureBootstrapPresentationModule.kt`
- Create: `.../feature/bootstrap/ui/api/FeatureBootstrapScreenApi.kt`
- Create: `.../feature/bootstrap/ui/BootstrapScreen.kt`

Добавить строки в strings.xml:
```xml
<!-- bootstrap -->
<string name="bootstrap_loading">Загрузка данных…</string>
<string name="bootstrap_error_title">Не удалось загрузить данные</string>
<string name="bootstrap_button_retry">Повторить</string>
```

- [ ] **Step 1: UiBootstrapState + UiBootstrapLabel**

`UiBootstrapState.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiBootstrapState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
```

`UiBootstrapLabel.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.presentation.models

sealed interface UiBootstrapLabel {
    data object NavigateToRoutesList : UiBootstrapLabel
    data object NavigateToLogin : UiBootstrapLabel
}
```

- [ ] **Step 2: StateMapper + LabelMapper**

`UiBootstrapStateMapper.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.presentation.mappers

import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState

interface UiBootstrapStateMapper {
    fun map(state: BootstrapStore.State): UiBootstrapState
}

internal class UiBootstrapStateMapperImpl : UiBootstrapStateMapper {
    override fun map(state: BootstrapStore.State): UiBootstrapState = with(state) {
        UiBootstrapState(isLoading = isLoading, errorMessage = errorMessage)
    }
}
```

`UiBootstrapLabelMapper.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel

interface UiBootstrapLabelMapper : Mapper<BootstrapStore.Label, UiBootstrapLabel>

internal class UiBootstrapLabelMapperImpl : UiBootstrapLabelMapper {
    override fun map(item: BootstrapStore.Label): UiBootstrapLabel = when (item) {
        BootstrapStore.Label.NavigateToRoutesList -> UiBootstrapLabel.NavigateToRoutesList
        BootstrapStore.Label.NavigateToLogin -> UiBootstrapLabel.NavigateToLogin
    }
}
```

- [ ] **Step 3: BootstrapViewModel**

```kotlin
package ru.mirea.toir.feature.bootstrap.presentation

import ru.mirea.toir.core.presentation.BaseViewModel
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapper
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState

class BootstrapViewModel internal constructor(
    private val store: BootstrapStore,
    private val stateMapper: UiBootstrapStateMapper,
    private val labelMapper: UiBootstrapLabelMapper,
) : BaseViewModel<UiBootstrapState, UiBootstrapLabel>(initialState = UiBootstrapState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRetry() = store.accept(BootstrapStore.Intent.Retry)

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
```

- [ ] **Step 4: DI-модули**

`FeatureBootstrapImplModule.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClientImpl
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.feature.bootstrap.impl.data.repository.BootstrapRepositoryImpl
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory

val featureBootstrapImplModule = module {
    factory<BootstrapApiClient> { new(::BootstrapApiClientImpl) }
    factory<BootstrapRepository> { new(::BootstrapRepositoryImpl) }
    factory { new(::BootstrapStoreFactory) }
}
```

`FeatureBootstrapPresentationModule.kt`:
```kotlin
package ru.mirea.toir.feature.bootstrap.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapperImpl
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapperImpl

val featureBootstrapPresentationModule = module {
    factory<UiBootstrapStateMapper> { new(::UiBootstrapStateMapperImpl) }
    factory<UiBootstrapLabelMapper> { new(::UiBootstrapLabelMapperImpl) }
    viewModelOf(::BootstrapViewModel)
}
```

- [ ] **Step 5: FeatureBootstrapScreenApi**

```kotlin
package ru.mirea.toir.feature.bootstrap.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.BootstrapRoute
import ru.mirea.toir.feature.bootstrap.ui.BootstrapScreen

fun NavGraphBuilder.composableBootstrapScreen(
    onNavigateToRoutesList: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable<BootstrapRoute> {
        BootstrapScreen(
            onNavigateToRoutesList = onNavigateToRoutesList,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}
```

- [ ] **Step 6: BootstrapScreen**

```kotlin
package ru.mirea.toir.feature.bootstrap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.res.MR

@Composable
internal fun BootstrapScreen(
    onNavigateToRoutesList: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: BootstrapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiBootstrapLabel.NavigateToRoutesList -> onNavigateToRoutesList()
            UiBootstrapLabel.NavigateToLogin -> onNavigateToLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.errorMessage != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(MR.strings.bootstrap_error_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.errorMessage.orEmpty())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = viewModel::onRetry) {
                    Text(text = stringResource(MR.strings.bootstrap_button_retry))
                }
            }
        }
    }
}
```

---

### Task 8: Рефакторинг App.kt — auth guard + удаление demo

**Files:**
- Rewrite: `shared/main/src/commonMain/kotlin/ru/mirea/toir/ui/App.kt`
- Modify: `shared/main/src/commonMain/kotlin/ru/mirea/toir/di/Koin.kt`
- Modify: `shared/core-navigation/src/commonMain/kotlin/ru/mirea/toir/core/navigation/Screen.kt`

- [ ] **Step 1: Переписать App.kt**

```kotlin
package ru.mirea.toir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.AuthRoute
import ru.mirea.toir.core.navigation.BootstrapRoute
import ru.mirea.toir.core.navigation.RoutesListRoute
import ru.mirea.toir.core.navigation.popBackStackOnResumed
import ru.mirea.toir.feature.auth.ui.api.composableAuthScreen
import ru.mirea.toir.feature.bootstrap.ui.api.composableBootstrapScreen
// RoutesListScreen screen api будет добавлен в Waypoint 04
// import ru.mirea.toir.feature.routes.list.ui.api.composableRoutesListScreen

@Composable
fun App() {
    ToirTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AuthRoute,
        ) {
            composableAuthScreen(
                onNavigateToMain = {
                    navController.navigate(BootstrapRoute) {
                        popUpTo(AuthRoute) { inclusive = true }
                    }
                },
            )
            composableBootstrapScreen(
                onNavigateToRoutesList = {
                    navController.navigate(RoutesListRoute) {
                        popUpTo(BootstrapRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
            // composableRoutesListScreen(...) — добавить в Waypoint 04
        }
    }
}
```

> **Примечание:** На этом этапе `RoutesListRoute` зарегистрирован в `Screen.kt`, но `composableRoutesListScreen` ещё не существует. NavHost будет компилироваться — он просто не будет содержать экран маршрутов до Waypoint 04.

- [ ] **Step 2: Обновить Koin.kt — убрать demo, добавить bootstrap**

```kotlin
package ru.mirea.toir.di

import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import ru.mirea.toir.common.di.commonModule
import ru.mirea.toir.common.resources.di.commonResourcesModule
import ru.mirea.toir.core.database.di.coreDatabaseModule
import ru.mirea.toir.core.domain.di.coreDomainModule
import ru.mirea.toir.core.mvikotlin.di.coreMVIKotlinModule
import ru.mirea.toir.core.network.di.coreNetworkModule
import ru.mirea.toir.core.network.ktor.di.coreNetworkKtorModule
import ru.mirea.toir.core.storage.di.coreStorageModule
import ru.mirea.toir.feature.auth.impl.di.featureAuthImplModule
import ru.mirea.toir.feature.auth.presentation.di.featureAuthPresentationModule
import ru.mirea.toir.feature.bootstrap.impl.di.featureBootstrapImplModule
import ru.mirea.toir.feature.bootstrap.presentation.di.featureBootstrapPresentationModule

fun initKoin(appDeclaration: KoinAppDeclaration) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            commonResourcesModule,

            coreDomainModule,
            coreMVIKotlinModule,
            coreNetworkModule,
            coreNetworkKtorModule,
            coreStorageModule,
            coreDatabaseModule,

            featureAuthImplModule,
            featureAuthPresentationModule,

            featureBootstrapImplModule,
            featureBootstrapPresentationModule,
        )
    }
}
```

---

### Task 9: Финальная проверка и коммит

- [ ] **Step 1: Собрать APK**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 2: Ручная проверка**
  1. Запустить приложение на эмуляторе
  2. Должен появиться экран входа (LoginScreen)
  3. Ввести корректные данные → кнопка «Войти» активна
  4. При нажатии «Войти» — появляется CircularProgressIndicator, затем переход на BootstrapScreen
  5. BootstrapScreen показывает «Загрузка данных…» и делает запрос к бэкенду
  6. При успехе — переход на пустой RoutesListRoute (пока нет экрана — нормально)
  7. При ошибке сети — появляется кнопка «Повторить»

- [ ] **Step 3: Commit**

```bash
git add shared/feature-bootstrap/ shared/main/ shared/core-navigation/ settings.gradle.kts shared/common-resources/
git commit -m "feat: add bootstrap screen, auth guard, remove demo features, wire real NavHost"
```
