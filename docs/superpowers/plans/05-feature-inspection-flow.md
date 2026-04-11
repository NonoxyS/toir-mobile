# Waypoint 05 — Inspection Flow (Точки маршрута + Карточка оборудования)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать два экрана: список точек маршрута в инспекции и карточку оборудования с кнопкой «Открыть чек-лист».

**Architecture:** `RoutePointsRoute(inspectionId)` → список `RoutePoint` с оборудованием и статусами `InspectionEquipmentResult`. Выбор точки → `EquipmentCardRoute(inspectionId, routePointId)` → карточка с кнопкой «Открыть чек-лист» → `ChecklistRoute(equipmentResultId)`. Кнопка «Завершить обход» закрывает инспекцию.

**Tech Stack:** MVIKotlin, SQLDelight (InspectionDao, RouteDao, EquipmentDao), Compose Multiplatform.

---

## Файловая структура

```
shared/feature-route-points/
├── api/.../feature/route/points/api/
│   ├── store/RoutePointsStore.kt
│   └── models/DomainRoutePoint.kt
├── impl/.../feature/route/points/impl/
│   ├── di/FeatureRoutePointsImplModule.kt
│   ├── domain/repository/RoutePointsRepository.kt       ← интерфейс в domain
│   ├── data/repository/RoutePointsRepositoryImpl.kt
│   └── domain/RoutePointsStoreFactory.kt  (+ Executor + Reducer)
├── presentation/.../feature/route/points/presentation/
│   ├── di/FeatureRoutePointsPresentationModule.kt
│   ├── models/UiRoutePointsState.kt
│   ├── models/UiRoutePointsLabel.kt
│   ├── models/UiRoutePoint.kt
│   ├── mappers/UiRoutePointsStateMapper.kt
│   ├── mappers/UiRoutePointsLabelMapper.kt
│   └── RoutePointsViewModel.kt
└── ui/.../feature/route/points/ui/
    ├── api/FeatureRoutePointsScreenApi.kt
    ├── RoutePointsScreen.kt
    └── RoutePointCard.kt

shared/feature-equipment-card/
├── api/.../feature/equipment/card/api/
│   ├── store/EquipmentCardStore.kt
│   └── models/DomainEquipmentCard.kt
├── impl/.../feature/equipment/card/impl/
│   ├── di/FeatureEquipmentCardImplModule.kt
│   ├── domain/repository/EquipmentCardRepository.kt    ← интерфейс в domain
│   ├── data/repository/EquipmentCardRepositoryImpl.kt
│   └── domain/EquipmentCardStoreFactory.kt  (+ Executor + Reducer)
├── presentation/.../feature/equipment/card/presentation/
│   ├── di/FeatureEquipmentCardPresentationModule.kt
│   ├── models/UiEquipmentCardState.kt
│   ├── models/UiEquipmentCardLabel.kt
│   ├── mappers/UiEquipmentCardStateMapper.kt
│   ├── mappers/UiEquipmentCardLabelMapper.kt
│   └── EquipmentCardViewModel.kt
└── ui/.../feature/equipment/card/ui/
    ├── api/FeatureEquipmentCardScreenApi.kt
    └── EquipmentCardScreen.kt
```

---

### Task 1: Регистрация, маршруты, build-файлы

- [ ] **Step 1: Добавить маршруты в Screen.kt**

```kotlin
@Serializable
data class EquipmentCardRoute(
    val inspectionId: String,
    val routePointId: String,
) : Screen

@Serializable
data class ChecklistRoute(val equipmentResultId: String) : Screen
```

- [ ] **Step 2: Зарегистрировать в settings.gradle.kts**

```kotlin
include(":shared:feature-route-points:api")
include(":shared:feature-route-points:impl")
include(":shared:feature-route-points:presentation")
include(":shared:feature-route-points:ui")

include(":shared:feature-equipment-card:api")
include(":shared:feature-equipment-card:impl")
include(":shared:feature-equipment-card:presentation")
include(":shared:feature-equipment-card:ui")
```

- [ ] **Step 3: Создать все build.gradle.kts файлы**

Для `feature-route-points` — по аналогии с `feature-routes-list` (impl зависит от `coreDatabase`).

`feature-route-points/api/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.route.points.api" }
```

`feature-route-points/impl/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.route.points.impl" }
commonMainDependencies {
    implementations(projects.shared.coreDatabase)
}
```

`feature-route-points/presentation/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.route.points.presentation" }
```

`feature-route-points/ui/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.route.points.ui" }
```

Аналогично для `feature-equipment-card` (замени `route.points` → `equipment.card`).

---

### Task 2: Feature Route Points — Store + Repository

**Files:**
- Create: `.../feature/route/points/api/models/DomainRoutePoint.kt`
- Create: `.../feature/route/points/api/store/RoutePointsStore.kt`
- Create: `.../feature/route/points/impl/domain/repository/RoutePointsRepository.kt`
- Create: `.../feature/route/points/impl/data/repository/RoutePointsRepositoryImpl.kt`

- [ ] **Step 1: DomainRoutePoint**

```kotlin
package ru.mirea.toir.feature.route.points.api.models

data class DomainRoutePoint(
    val routePointId: String,
    val equipmentId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val equipmentResultId: String?,
    val status: String,
    val hasIssues: Boolean,
)
```

- [ ] **Step 2: RoutePointsStore**

```kotlin
package ru.mirea.toir.feature.route.points.api.store

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

interface RoutePointsStore : Store<RoutePointsStore.Intent, RoutePointsStore.State, RoutePointsStore.Label> {

    data class State(
        val inspectionId: String = "",
        val routeName: String = "",
        val points: ImmutableList<DomainRoutePoint> = persistentListOf(),
        val isLoading: Boolean = true,
        val canFinish: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class Init(val inspectionId: String) : Intent
        data class OnPointClick(val routePointId: String) : Intent
        data object OnFinishInspection : Intent
    }

    sealed interface Label {
        data class NavigateToEquipmentCard(val inspectionId: String, val routePointId: String) : Label
        data object InspectionFinished : Label
    }
}
```

- [ ] **Step 3: RoutePointsRepository**

```kotlin
package ru.mirea.toir.feature.route.points.impl.data.repository

import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

internal interface RoutePointsRepository {
    suspend fun getRoutePoints(inspectionId: String): Result<Pair<String, List<DomainRoutePoint>>>
    suspend fun finishInspection(inspectionId: String): Result<Unit>
}
```

- [ ] **Step 4: RoutePointsRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.route.points.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

internal class RoutePointsRepositoryImpl(
    private val inspectionDao: InspectionDao,
    private val routeDao: RouteDao,
    private val equipmentDao: EquipmentDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutePointsRepository {

    override suspend fun getRoutePoints(inspectionId: String): Result<Pair<String, List<DomainRoutePoint>>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val inspection = inspectionDao.selectById(inspectionId)
                        ?: error("Inspection not found: $inspectionId")
                    val route = routeDao.selectRouteById(inspection.route_id)
                    val routePoints = routeDao.selectPointsByRouteId(inspection.route_id)
                    val equipmentResults = inspectionDao.selectEquipmentResultsByInspectionId(inspectionId)

                    val points = routePoints.map { point ->
                        val equipment = equipmentDao.selectById(point.equipment_id)
                        val result = equipmentResults.firstOrNull { it.route_point_id == point.id }
                        DomainRoutePoint(
                            routePointId = point.id,
                            equipmentId = point.equipment_id,
                            equipmentCode = equipment?.code.orEmpty(),
                            equipmentName = equipment?.name.orEmpty(),
                            locationName = equipment?.location_id.orEmpty(),
                            equipmentResultId = result?.id,
                            status = result?.status ?: "NOT_STARTED",
                            hasIssues = result?.status == "SKIPPED",
                        )
                    }

                    (route?.name.orEmpty() to points).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getRoutePoints failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun finishInspection(inspectionId: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    inspectionDao.updateInspectionStatus(
                        id = inspectionId,
                        status = "COMPLETED",
                        completedAt = Clock.System.now().toString(),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "finishInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

---

### Task 3: RoutePoints — Executor, Reducer, StoreFactory, DI

**Files:**
- Create: `.../feature/route/points/impl/domain/RoutePointsReducer.kt`
- Create: `.../feature/route/points/impl/domain/RoutePointsExecutor.kt`
- Create: `.../feature/route/points/impl/domain/RoutePointsStoreFactory.kt`
- Create: `.../feature/route/points/impl/di/FeatureRoutePointsImplModule.kt`

- [ ] **Step 1: RoutePointsReducer**

```kotlin
package ru.mirea.toir.feature.route.points.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore

internal class RoutePointsReducer : Reducer<RoutePointsStore.State, RoutePointsStoreFactory.Message> {
    override fun RoutePointsStore.State.reduce(msg: RoutePointsStoreFactory.Message): RoutePointsStore.State = when (msg) {
        RoutePointsStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is RoutePointsStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        is RoutePointsStoreFactory.Message.SetData -> copy(
            isLoading = false,
            inspectionId = msg.inspectionId,
            routeName = msg.routeName,
            points = msg.points,
            canFinish = msg.points.any { it.status == "COMPLETED" },
        )
    }
}
```

- [ ] **Step 2: RoutePointsExecutor**

```kotlin
package ru.mirea.toir.feature.route.points.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.impl.data.repository.RoutePointsRepository

internal class RoutePointsExecutor(
    private val repository: RoutePointsRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<RoutePointsStore.Intent, Nothing, RoutePointsStore.State, RoutePointsStoreFactory.Message, RoutePointsStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: RoutePointsStore.Intent) {
        when (intent) {
            is RoutePointsStore.Intent.Init -> loadPoints(intent.inspectionId)
            is RoutePointsStore.Intent.OnPointClick -> publish(
                RoutePointsStore.Label.NavigateToEquipmentCard(state().inspectionId, intent.routePointId)
            )
            RoutePointsStore.Intent.OnFinishInspection -> finishInspection()
        }
    }

    private suspend fun loadPoints(inspectionId: String) {
        dispatch(RoutePointsStoreFactory.Message.SetLoading)
        repository.getRoutePoints(inspectionId).fold(
            onSuccess = { (routeName, points) ->
                dispatch(RoutePointsStoreFactory.Message.SetData(
                    inspectionId = inspectionId,
                    routeName = routeName,
                    points = points.toImmutableList(),
                ))
            },
            onFailure = { throwable ->
                dispatch(RoutePointsStoreFactory.Message.SetError(throwable.message ?: "Ошибка"))
            },
        )
    }

    private suspend fun finishInspection() {
        val inspectionId = state().inspectionId
        repository.finishInspection(inspectionId).fold(
            onSuccess = { publish(RoutePointsStore.Label.InspectionFinished) },
            onFailure = { throwable ->
                dispatch(RoutePointsStoreFactory.Message.SetError(throwable.message ?: "Ошибка завершения"))
            },
        )
    }
}
```

- [ ] **Step 3: RoutePointsStoreFactory**

```kotlin
package ru.mirea.toir.feature.route.points.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.impl.data.repository.RoutePointsRepository

internal class RoutePointsStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RoutePointsRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): RoutePointsStore =
        object :
            RoutePointsStore,
            Store<RoutePointsStore.Intent, RoutePointsStore.State, RoutePointsStore.Label> by storeFactory.create(
                name = RoutePointsStore::class.simpleName,
                initialState = RoutePointsStore.State(),
                bootstrapper = null,
                executorFactory = { RoutePointsExecutor(repository, mainDispatcher) },
                reducer = RoutePointsReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data class SetData(
            val inspectionId: String,
            val routeName: String,
            val points: ImmutableList<DomainRoutePoint>,
        ) : Message
    }
}
```

- [ ] **Step 4: DI**

```kotlin
package ru.mirea.toir.feature.route.points.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.route.points.impl.data.repository.RoutePointsRepository
import ru.mirea.toir.feature.route.points.impl.data.repository.RoutePointsRepositoryImpl
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory

val featureRoutePointsImplModule = module {
    factory<RoutePointsRepository> { new(::RoutePointsRepositoryImpl) }
    factory { new(::RoutePointsStoreFactory) }
}
```

---

### Task 4: RoutePoints — Presentation + UI

> Паттерн полностью аналогичен RoutesListViewModel. Ниже — сжатая версия.

- [ ] **Step 1: UiRoutePoint + UiRoutePointsState + UiRoutePointsLabel**

`UiRoutePoint.kt`:
```kotlin
package ru.mirea.toir.feature.route.points.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiRoutePoint(
    val routePointId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val statusLabel: String,
    val statusColor: String, // "default" | "success" | "warning" | "error"
    val hasIssues: Boolean,
    val equipmentResultId: String?,
)
```

`UiRoutePointsState.kt`:
```kotlin
package ru.mirea.toir.feature.route.points.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiRoutePointsState(
    val routeName: String = "",
    val points: ImmutableList<UiRoutePoint> = persistentListOf(),
    val isLoading: Boolean = true,
    val canFinish: Boolean = false,
    val errorMessage: String? = null,
)
```

`UiRoutePointsLabel.kt`:
```kotlin
package ru.mirea.toir.feature.route.points.presentation.models

sealed interface UiRoutePointsLabel {
    data class NavigateToEquipmentCard(val inspectionId: String, val routePointId: String) : UiRoutePointsLabel
    data object InspectionFinished : UiRoutePointsLabel
}
```

- [ ] **Step 2: StateMapper + LabelMapper + ViewModel + DI (presentation)**

Создать `UiRoutePointsStateMapper`, `UiRoutePointsLabelMapper`, `RoutePointsViewModel`, `FeatureRoutePointsPresentationModule` по аналогии с RoutesList. В StateMapper маппинг `DomainRoutePoint.status` → `statusLabel`:
```kotlin
"NOT_STARTED" -> "Не начато"
"IN_PROGRESS" -> "В работе"
"COMPLETED" -> "Завершено"
"SKIPPED" -> "Пропущено"
else -> status
```

- [ ] **Step 3: FeatureRoutePointsScreenApi**

```kotlin
package ru.mirea.toir.feature.route.points.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.RoutePointsRoute
import ru.mirea.toir.feature.route.points.ui.RoutePointsScreen

fun NavGraphBuilder.composableRoutePointsScreen(
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinished: () -> Unit,
) {
    composable<RoutePointsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RoutePointsRoute>()
        RoutePointsScreen(
            inspectionId = route.inspectionId,
            onNavigateToEquipmentCard = onNavigateToEquipmentCard,
            onInspectionFinished = onInspectionFinished,
        )
    }
}
```

- [ ] **Step 4: Добавить строки в strings.xml**

```xml
<!-- route_points -->
<string name="route_points_button_finish">Завершить обход</string>
<string name="route_points_status_not_started">Не начато</string>
<string name="route_points_status_in_progress">В работе</string>
<string name="route_points_status_completed">Завершено</string>
<string name="route_points_status_skipped">Пропущено</string>
```

- [ ] **Step 5: RoutePointsScreen (базовая реализация)**

```kotlin
package ru.mirea.toir.feature.route.points.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.route.points.presentation.RoutePointsViewModel
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsLabel
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutePointsScreen(
    inspectionId: String,
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinished: () -> Unit,
    viewModel: RoutePointsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(inspectionId) {
        viewModel.init(inspectionId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiRoutePointsLabel.NavigateToEquipmentCard ->
                onNavigateToEquipmentCard(label.inspectionId, label.routePointId)
            UiRoutePointsLabel.InspectionFinished -> onInspectionFinished()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = state.routeName) }) },
        bottomBar = {
            if (state.canFinish) {
                Button(
                    onClick = viewModel::onFinishInspection,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(text = stringResource(MR.strings.route_points_button_finish))
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = state.points, key = { it.routePointId }) { point ->
                        RoutePointCard(
                            item = point,
                            onClick = { viewModel.onPointClick(point.routePointId) },
                        )
                    }
                }
            }
        }
    }
}
```

---

### Task 5: Feature Equipment Card

> Паттерн аналогичен другим фичам. Ниже — ключевые файлы.

**Files:**
- Create: `DomainEquipmentCard.kt`, `EquipmentCardStore.kt`, `EquipmentCardRepository.kt`, `EquipmentCardRepositoryImpl.kt`, `EquipmentCardStoreFactory.kt` (+ Executor + Reducer), presentation + UI модули.

- [ ] **Step 1: DomainEquipmentCard + EquipmentCardStore**

`DomainEquipmentCard.kt`:
```kotlin
package ru.mirea.toir.feature.equipment.card.api.models

data class DomainEquipmentCard(
    val equipmentId: String,
    val code: String,
    val name: String,
    val type: String,
    val locationName: String,
    val equipmentResultId: String,
    val inspectionStatus: String,
)
```

`EquipmentCardStore.kt`:
```kotlin
package ru.mirea.toir.feature.equipment.card.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

interface EquipmentCardStore : Store<EquipmentCardStore.Intent, EquipmentCardStore.State, EquipmentCardStore.Label> {

    data class State(
        val card: DomainEquipmentCard? = null,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class Init(val inspectionId: String, val routePointId: String) : Intent
        data object OnOpenChecklist : Intent
    }

    sealed interface Label {
        data class NavigateToChecklist(val equipmentResultId: String) : Label
    }
}
```

- [ ] **Step 2: EquipmentCardRepository**

```kotlin
package ru.mirea.toir.feature.equipment.card.impl.data.repository

import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

internal interface EquipmentCardRepository {
    suspend fun getOrCreateEquipmentResult(inspectionId: String, routePointId: String): Result<DomainEquipmentCard>
}
```

- [ ] **Step 3: EquipmentCardRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.equipment.card.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class EquipmentCardRepositoryImpl(
    private val inspectionDao: InspectionDao,
    private val routeDao: RouteDao,
    private val equipmentDao: EquipmentDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : EquipmentCardRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getOrCreateEquipmentResult(
        inspectionId: String,
        routePointId: String,
    ): Result<DomainEquipmentCard> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val routePoint = routeDao.selectPointById(routePointId)
                        ?: error("RoutePoint not found: $routePointId")
                    val equipment = equipmentDao.selectById(routePoint.equipment_id)
                        ?: error("Equipment not found: ${routePoint.equipment_id}")

                    var result = inspectionDao.selectEquipmentResultByRoutePoint(routePointId, inspectionId)
                    if (result == null) {
                        val newId = Uuid.random().toString()
                        inspectionDao.insertEquipmentResult(
                            id = newId,
                            inspectionId = inspectionId,
                            routePointId = routePointId,
                            equipmentId = equipment.id,
                            status = "IN_PROGRESS",
                        )
                        result = inspectionDao.selectEquipmentResultById(newId)
                            ?: error("Failed to create equipment result")
                    }

                    DomainEquipmentCard(
                        equipmentId = equipment.id,
                        code = equipment.code,
                        name = equipment.name,
                        type = equipment.type,
                        locationName = equipment.location_id,
                        equipmentResultId = result.id,
                        inspectionStatus = result.status,
                    ).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getOrCreateEquipmentResult failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

- [ ] **Step 4: EquipmentCardExecutor + EquipmentCardReducer + EquipmentCardStoreFactory**

Следуй тому же паттерну, что и для RoutePoints. `Init` → загружает карточку через репозиторий. `OnOpenChecklist` → публикует `Label.NavigateToChecklist(equipmentResultId)`.

- [ ] **Step 5: UI EquipmentCardScreen**

```kotlin
// В EquipmentCardScreen: показать поля code, name, type, locationName + кнопку «Открыть чек-лист»
```

Добавить строки в strings.xml:
```xml
<!-- equipment_card -->
<string name="equipment_card_code">Код</string>
<string name="equipment_card_name">Наименование</string>
<string name="equipment_card_type">Тип</string>
<string name="equipment_card_location">Локация</string>
<string name="equipment_card_button_open_checklist">Открыть чек-лист</string>
<string name="equipment_card_status_not_checked">Оборудование не проверено</string>
<string name="equipment_card_status_in_progress">Проверка в процессе</string>
<string name="equipment_card_status_checked">Оборудование проверено</string>
```

---

### Task 6: Подключить в App.kt и Koin

- [ ] **Step 1: Добавить модули в Koin.kt**

```kotlin
import ru.mirea.toir.feature.route.points.impl.di.featureRoutePointsImplModule
import ru.mirea.toir.feature.route.points.presentation.di.featureRoutePointsPresentationModule
import ru.mirea.toir.feature.equipment.card.impl.di.featureEquipmentCardImplModule
import ru.mirea.toir.feature.equipment.card.presentation.di.featureEquipmentCardPresentationModule
// ...
featureRoutePointsImplModule,
featureRoutePointsPresentationModule,
featureEquipmentCardImplModule,
featureEquipmentCardPresentationModule,
```

- [ ] **Step 2: Добавить экраны в NavHost (App.kt)**

```kotlin
import ru.mirea.toir.core.navigation.EquipmentCardRoute
import ru.mirea.toir.feature.route.points.ui.api.composableRoutePointsScreen
import ru.mirea.toir.feature.equipment.card.ui.api.composableEquipmentCardScreen

composableRoutePointsScreen(
    onNavigateToEquipmentCard = { inspectionId, routePointId ->
        navController.navigate(EquipmentCardRoute(inspectionId, routePointId))
    },
    onInspectionFinished = { navController.popBackStack(RoutesListRoute, inclusive = false) },
)

composableEquipmentCardScreen(
    onNavigateToChecklist = { equipmentResultId ->
        navController.navigate(ChecklistRoute(equipmentResultId))
    },
    onNavigateBack = navController::popBackStackOnResumed,
)
```

- [ ] **Step 3: Сборка и ручная проверка**

```bash
./gradlew :android:app:assembleDebug
```

Проверить:
1. Из списка маршрутов → точки маршрута (список точек с иконками статуса)
2. Нажать на точку → карточка оборудования (поля: код, название, тип, локация)
3. Кнопка «Открыть чек-лист» — переход на `ChecklistRoute` (пока нет экрана — нормально)
4. Кнопка «Завершить обход» появляется, когда есть хоть одна завершённая точка

- [ ] **Step 4: Commit**

```bash
git add shared/feature-route-points/ shared/feature-equipment-card/ shared/main/ shared/core-navigation/ settings.gradle.kts shared/common-resources/
git commit -m "feat: implement route points list and equipment card screens"
```
