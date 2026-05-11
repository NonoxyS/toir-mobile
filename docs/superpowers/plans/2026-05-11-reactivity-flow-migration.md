# Reactivity Migration to SQLDelight Flows + RoutePoints Back Button Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Переход всех экранов inspection-flow на реактивные подписки SQLDelight `asFlow().mapToList(...)` вместо разовых suspend-вызовов из репозиториев; удаление временного `LifecycleEventEffect(ON_RESUME) { onRefresh() }` в RoutesListScreen; добавление back-кнопки на RoutePointsScreen.

**Architecture:**
- В Storage (`core-database`) к каждому существующему `selectX(): List<T>` добавляется парный `observeX(): Flow<List<T>>` через `queries.selectX().asFlow().mapToList(dispatchers.io)`. Existing suspend-методы оставляем (используются для одноразовых write/lookup).
- В каждом feature-репозитории контракт `suspend fun getX(): Result<…>` заменяется на `fun observeX(): Flow<…>`. Projection-логика (типа `resolveEffectiveStatus`) переезжает внутрь `combine(...) { … }` нескольких storage-flow.
- В Executor разовый `repository.getX()` заменяется на:
  ```kotlin
  repository.observeX()
      .onStart { dispatch(Message.SetLoading) }
      .onEach { dispatch(Message.SetLoaded(it)) }
      .catch { dispatch(Message.SetError) }
      .launchIn(scope)
  ```
  Подписка живёт пока живёт `scope` Executor'а — то есть пока NavBackStackEntry держит ViewModel/Store. При уходе пользователя с экрана и пропадания записи из back stack Store диспозится → подписка отваливается. Семантически это эквивалент WhileSubscribed для нашей MVIKotlin-схемы; буквальный `SharingStarted.WhileSubscribed(5_000)` тут негде применить, потому что между Store и UI нет промежуточного `stateIn`.

**Tech Stack:** Kotlin Multiplatform, MVIKotlin, SQLDelight 2.3.2 (`coroutines-extensions` уже подключён в `core-database/build.gradle.kts:28`), Compose Multiplatform, Koin.

---

## File Structure

### Storage layer (`shared/core-database`)
- Modify: `src/commonMain/kotlin/.../storage/route/RouteStorage.kt` — добавить `observeAllAssignments`, `observeAssignmentById`, `observePointsByRouteId`.
- Modify: `src/commonMain/kotlin/.../storage/route/RouteStorageImpl.kt` — реализации через `asFlow().mapToList`.
- Modify: `src/commonMain/kotlin/.../storage/inspection/InspectionStorage.kt` — добавить `observeInspectionByAssignmentId`, `observeEquipmentResultsByInspectionId`, `observeEquipmentResultByRoutePoint`, `observeEquipmentResultById`, `observeChecklistItemResultsByEquipmentResult`.
- Modify: `src/commonMain/kotlin/.../storage/inspection/InspectionStorageImpl.kt` — реализации.
- Modify: `src/commonMain/kotlin/.../storage/equipment/EquipmentStorage.kt` + `EquipmentStorageImpl.kt` — `observeEquipmentById`.
- Modify: `src/commonMain/kotlin/.../storage/checklist/ChecklistStorage.kt` + `ChecklistStorageImpl.kt` — `observeItemsByChecklistId`.

### RoutesList feature
- Modify: `shared/feature-routes-list/impl/.../domain/repository/RoutesListRepository.kt` — `getAssignments` → `observeAssignments(): Flow<List<DomainRouteAssignment>>`.
- Modify: `shared/feature-routes-list/impl/.../data/repository/RoutesListRepositoryImpl.kt` — combine из 4 storage-flow.
- Modify: `shared/feature-routes-list/impl/.../domain/RoutesListExecutor.kt` — подписка в `suspendExecuteAction`.
- Modify: `shared/feature-routes-list/ui/.../RoutesListScreen.kt` — удалить `LifecycleEventEffect(ON_RESUME) { viewModel.onRefresh() }` и неиспользуемые импорты.

### RoutePoints feature
- Modify: `shared/feature-route-points/impl/.../domain/repository/RoutePointsRepository.kt` — `getRoutePoints` → `observeRoutePoints(inspectionId): Flow<Pair<String, List<DomainRoutePoint>>>`.
- Modify: `shared/feature-route-points/impl/.../data/repository/RoutePointsRepositoryImpl.kt`.
- Modify: `shared/feature-route-points/impl/.../domain/RoutePointsExecutor.kt`.
- Modify: `shared/feature-route-points/ui/.../api/FeatureRoutePointsScreenApi.kt` — добавить `onNavigateBack` параметр в `composableRoutePointsScreen`.
- Modify: `shared/feature-route-points/ui/.../RoutePointsScreen.kt` — добавить `onNavigateBack` параметр и `navigationIcon` в `RoutePointsTopBar`.
- Modify: `shared/main/.../ui/App.kt` — пробросить `onNavigateBack = { navController.popBackStack() }`.

### EquipmentCard feature
- Modify: `shared/feature-equipment-card/impl/.../domain/repository/EquipmentCardRepository.kt` — `getEquipmentCard` → `observeEquipmentCard(inspectionId, routePointId): Flow<DomainEquipmentCard>`.
- Modify: `shared/feature-equipment-card/impl/.../data/repository/EquipmentCardRepositoryImpl.kt`.
- Modify: `shared/feature-equipment-card/impl/.../domain/EquipmentCardExecutor.kt`.

### Checklist feature
- Modify: `shared/feature-checklist/impl/.../domain/repository/ChecklistRepository.kt` — добавить `observeChecklist(equipmentResultId): Flow<DomainChecklist>`. Write-методы (`saveAnswer`, `markCompleted`) остаются suspend.
- Modify: `shared/feature-checklist/impl/.../data/repository/ChecklistRepositoryImpl.kt`.
- Modify: `shared/feature-checklist/impl/.../domain/ChecklistExecutor.kt`.

### Memory cleanup
- Delete entry: `/Users/a.dobrov/.claude/projects/-Users-a-dobrov-StudioProjects-toir-mobile/memory/feedback_reactivity_whileSubscribed.md` line "Уже добавленный фикс в RoutesListScreen…" — заменить на запись, что миграция выполнена.

---

## Verification commands

Используются в каждой задаче. Запускать из корня репозитория.

```bash
./gradlew detekt
./gradlew :android:app:assembleDebug
```

Ожидаем `BUILD SUCCESSFUL`. Для feature-модулей с тестами:

```bash
./gradlew :shared:feature-routes-list:impl:allTests
./gradlew :shared:feature-route-points:impl:allTests
./gradlew :shared:feature-equipment-card:impl:allTests
./gradlew :shared:feature-checklist:impl:allTests
```

---

## Tasks

### Task 1: Back button на RoutePointsScreen (изолированный фикс)

**Files:**
- Modify: `shared/feature-route-points/ui/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/ui/api/FeatureRoutePointsScreenApi.kt:28-40`
- Modify: `shared/feature-route-points/ui/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/ui/RoutePointsScreen.kt:47-118`
- Modify: `shared/main/src/commonMain/kotlin/ru/mirea/toir/ui/App.kt:51-58`

#### Шаги

- [ ] **Step 1.1: Расширить `composableRoutePointsScreen` параметром `onNavigateBack`**

Открыть `FeatureRoutePointsScreenApi.kt`. Заменить `composableRoutePointsScreen` на:

```kotlin
fun NavGraphBuilder.composableRoutePointsScreen(
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinish: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<RoutePointsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RoutePointsRoute>()
        RoutePointsScreen(
            inspectionId = route.inspectionId,
            onNavigateToEquipmentCard = onNavigateToEquipmentCard,
            onInspectionFinish = onInspectionFinish,
            onNavigateBack = onNavigateBack,
        )
    }
}
```

- [ ] **Step 1.2: Добавить `onNavigateBack` в `RoutePointsScreen` signature**

В `RoutePointsScreen.kt` заменить блок параметров (строки 47-52):

```kotlin
internal fun RoutePointsScreen(
    inspectionId: String,
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinish: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RoutePointsViewModel = koinViewModel { parametersOf(inspectionId) },
) {
```

И пробросить в `topBar`:

```kotlin
topBar = { RoutePointsTopBar(state = state, onNavigateBack = onNavigateBack) },
```

- [ ] **Step 1.3: Отрендерить кнопку в `RoutePointsTopBar`**

Заменить `RoutePointsTopBar` (строки 101-118) на:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutePointsTopBar(
    state: UiRoutePointsState,
    onNavigateBack: () -> Unit,
) {
    val colors = ToirTheme.colors
    Column {
        TopAppBar(
            title = {
                Text(
                    text = state.routeName,
                    style = ToirTheme.typography.headline,
                    color = colors.textPrimary,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Image(
                        painter = painterResource(MR.images.ic_arrow_back),
                        contentDescription = stringResource(
                            MR.strings.route_points_back_content_description,
                        ),
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(colors.textSecondary),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background,
            ),
        )
        RoutePointsProgressHeader(state = state)
    }
}
```

Добавить недостающие импорты в верх файла:

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.ColorFilter
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
```

`stringResource` уже импортирован — пропустить дубликат.

- [ ] **Step 1.4: Добавить строку `route_points_back_content_description` в moko-resources**

Найти ресурсный файл со строкой `equipment_card_back_content_description` и добавить рядом `route_points_back_content_description`.

Найти файл:

```bash
grep -rn "equipment_card_back_content_description" /Users/a.dobrov/StudioProjects/toir-mobile --include="*.xml"
```

Открыть найденный XML и добавить рядом:

```xml
<string name="route_points_back_content_description">Назад к списку маршрутов</string>
```

- [ ] **Step 1.5: Пробросить `onNavigateBack` в App.kt**

В `shared/main/src/commonMain/kotlin/ru/mirea/toir/ui/App.kt` заменить блок `composableRoutePointsScreen` (строки 51-58):

```kotlin
composableRoutePointsScreen(
    onNavigateToEquipmentCard = { inspectionId, routePointId ->
        navController.navigateToEquipmentCardScreen(inspectionId, routePointId)
    },
    onInspectionFinish = {
        navController.popBackStack(RoutesListRoute, inclusive = false)
    },
    onNavigateBack = { navController.popBackStack() },
)
```

- [ ] **Step 1.6: Build verification**

```bash
./gradlew detekt :android:app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Если detekt ругается на `ComposableParametersOrdering` — переставить параметры так, чтобы все non-Composable lambda шли подряд (поправить и пересобрать).

- [ ] **Step 1.7: Commit**

```bash
git add shared/feature-route-points shared/main shared/common-resources
git commit -m "$(cat <<'EOF'
feat(route-points): add back button to RoutePointsScreen

Add onNavigateBack callback through composableRoutePointsScreen → RoutePointsScreen
→ RoutePointsTopBar.navigationIcon. App.kt wires popBackStack().

Resolves missing back navigation observed in user UX audit.
EOF
)"
```

---

### Task 2: Storage layer — добавить observe-методы

**Files:**
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/route/RouteStorage.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/route/RouteStorageImpl.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/InspectionStorage.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/InspectionStorageImpl.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/equipment/EquipmentStorage.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/equipment/EquipmentStorageImpl.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/checklist/ChecklistStorage.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/checklist/ChecklistStorageImpl.kt`

`Impl` теперь нужен `CoroutineDispatchers` для `mapToList(io)`. Сейчас Impl принимает только `db: ToirDatabase`. Меняем конструктор каждого StorageImpl и Koin-биндинг в `core-database` DI.

#### Шаги

- [ ] **Step 2.1: Найти Koin-биндинги core-database**

```bash
find /Users/a.dobrov/StudioProjects/toir-mobile/shared/core-database -name "*Module.kt" -path "*/di/*"
```

Прочитать найденный файл (`CoreDatabaseModule.kt` или подобный) — там увидим как создаются `RouteStorageImpl(db)` и т.д.

- [ ] **Step 2.2: Добавить методы в `RouteStorage.kt`**

В interface `RouteStorage`:

```kotlin
import kotlinx.coroutines.flow.Flow

fun observeAllAssignments(): Flow<List<LocalRouteAssignment>>

fun observeAssignmentById(id: String): Flow<LocalRouteAssignment?>

fun observePointsByRouteId(routeId: String): Flow<List<LocalRoutePoint>>
```

- [ ] **Step 2.3: Реализовать в `RouteStorageImpl.kt`**

Изменить конструктор:

```kotlin
internal class RouteStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : RouteStorage {
```

(Импортировать `ru.mirea.toir.common.coroutines.CoroutineDispatchers`.)

Добавить методы внизу класса:

```kotlin
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

override fun observeAllAssignments(): Flow<List<LocalRouteAssignment>> =
    assignmentQueries.selectAll().asFlow().mapToList(dispatchers.io).map { list ->
        list.map { it.toLocal() }
    }

override fun observeAssignmentById(id: String): Flow<LocalRouteAssignment?> =
    assignmentQueries.selectById(id).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toLocal() }

override fun observePointsByRouteId(routeId: String): Flow<List<LocalRoutePoint>> =
    pointQueries.selectByRouteId(routeId).asFlow().mapToList(dispatchers.io).map { list ->
        list.map { it.toLocal() }
    }
```

`.toLocal()` — приватная extension-функция, уже используется в существующем `selectAllAssignments` (строка 23 и далее). Найти её сигнатуру при необходимости через `grep -n "toLocal" RouteStorageImpl.kt`.

- [ ] **Step 2.4: Добавить методы в `InspectionStorage.kt`**

```kotlin
import kotlinx.coroutines.flow.Flow

fun observeInspectionByAssignmentId(assignmentId: String): Flow<LocalInspection?>

fun observeEquipmentResultsByInspectionId(inspectionId: String): Flow<List<LocalEquipmentResult>>

fun observeEquipmentResultByRoutePoint(
    routePointId: String,
    inspectionId: String,
): Flow<LocalEquipmentResult?>

fun observeEquipmentResultById(id: String): Flow<LocalEquipmentResult?>

fun observeChecklistItemResultsByEquipmentResult(
    equipmentResultId: String,
): Flow<List<LocalChecklistItemResult>>
```

- [ ] **Step 2.5: Реализовать в `InspectionStorageImpl.kt`**

Аналогично Step 2.3: добавить `dispatchers: CoroutineDispatchers` в конструктор и реализовать пять Flow-методов через `asFlow().mapToList(io)` / `.mapToOneOrNull(io)` + `.map { ... toLocal() }`. Использовать существующие queries:
- `inspectionQueries.selectByAssignmentId(assignmentId)`
- `equipmentResultQueries.selectByInspectionId(inspectionId)`
- `equipmentResultQueries.selectByRoutePoint(routePointId, inspectionId)`
- `equipmentResultQueries.selectById(id)`
- `checklistItemResultQueries.selectByEquipmentResult(equipmentResultId)`

Имена queries сверять с существующими suspend-методами в Impl (строки выше `insert*`/`select*`).

- [ ] **Step 2.6: Добавить и реализовать `observeEquipmentById` в `EquipmentStorage` / `EquipmentStorageImpl`**

```kotlin
// EquipmentStorage.kt
fun observeEquipmentById(id: String): Flow<LocalEquipment?>

// EquipmentStorageImpl.kt — после правки конструктора на CoroutineDispatchers
override fun observeEquipmentById(id: String): Flow<LocalEquipment?> =
    equipmentQueries.selectById(id).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toLocal() }
```

- [ ] **Step 2.7: Добавить и реализовать `observeItemsByChecklistId` в `ChecklistStorage` / `ChecklistStorageImpl`**

```kotlin
// ChecklistStorage.kt
fun observeItemsByChecklistId(checklistId: String): Flow<List<LocalChecklistItem>>

// ChecklistStorageImpl.kt
override fun observeItemsByChecklistId(checklistId: String): Flow<List<LocalChecklistItem>> =
    itemQueries.selectByChecklistId(checklistId).asFlow().mapToList(dispatchers.io).map { list ->
        list.map { it.toLocal() }
    }
```

- [ ] **Step 2.8: Обновить Koin-модуль `core-database`**

В найденном на Step 2.1 файле модуля, каждое биндинг типа:

```kotlin
single<RouteStorage> { RouteStorageImpl(get()) }
```

заменить на:

```kotlin
single<RouteStorage> { RouteStorageImpl(get(), get()) }
```

(второй `get()` — `CoroutineDispatchers`.) Сделать для `RouteStorageImpl`, `InspectionStorageImpl`, `EquipmentStorageImpl`, `ChecklistStorageImpl`.

- [ ] **Step 2.9: Build verification**

```bash
./gradlew :shared:core-database:compileCommonMainKotlinMetadata
```

Expected: `BUILD SUCCESSFUL`. Любые red-imports внутри Impl-файлов исправить — `app.cash.sqldelight.coroutines.*` пакет.

- [ ] **Step 2.10: Commit**

```bash
git add shared/core-database
git commit -m "$(cat <<'EOF'
feat(core-database): add Flow-based observe* methods to storage interfaces

Wire SQLDelight coroutines-extensions (asFlow/mapToList/mapToOneOrNull) through
RouteStorage, InspectionStorage, EquipmentStorage, ChecklistStorage. Existing
suspend selectX methods kept for one-shot writes/lookups; new observeX methods
are used by feature repositories.

Constructor of every StorageImpl now takes CoroutineDispatchers to pass io
dispatcher to mapToList.
EOF
)"
```

---

### Task 3: RoutesList feature — переход на Flow

**Files:**
- Modify: `shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/domain/repository/RoutesListRepository.kt`
- Modify: `shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/data/repository/RoutesListRepositoryImpl.kt`
- Modify: `shared/feature-routes-list/impl/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/impl/domain/RoutesListExecutor.kt`
- Modify: `shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/RoutesListScreen.kt`
- Test: `shared/feature-routes-list/impl/src/commonTest/kotlin/.../RoutesListReducerTest.kt` (если будет затронут) — оставляем как есть, контракт reducer не меняется.

#### Шаги

- [ ] **Step 3.1: Изменить контракт `RoutesListRepository.kt`**

Заменить файл полностью:

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

internal interface RoutesListRepository {
    fun observeAssignments(): Flow<List<DomainRouteAssignment>>
    suspend fun startInspection(assignmentId: String): Result<String>
}
```

- [ ] **Step 3.2: Переписать `RoutesListRepositoryImpl.kt`**

Поменять `getAssignments` на `observeAssignments` с `combine`:

```kotlin
override fun observeAssignments(): Flow<List<DomainRouteAssignment>> {
    val assignmentsFlow = routeStorage.observeAllAssignments()
    return assignmentsFlow.flatMapLatest { assignments ->
        if (assignments.isEmpty()) {
            flowOf(emptyList())
        } else {
            val perAssignmentFlows = assignments.map { assignment ->
                buildAssignmentFlow(assignment)
            }
            combine(perAssignmentFlows) { it.toList() }
        }
    }.flowOn(coroutineDispatchers.io)
}

private fun buildAssignmentFlow(
    assignment: LocalRouteAssignment,
): Flow<DomainRouteAssignment> {
    val route = routeStorage.selectRouteById(assignment.routeId)
    val pointsFlow = routeStorage.observePointsByRouteId(assignment.routeId)
    val inspectionFlow = inspectionStorage.observeInspectionByAssignmentId(assignment.id)
    return inspectionFlow.flatMapLatest { inspection ->
        val equipmentResultsFlow = if (inspection != null) {
            inspectionStorage.observeEquipmentResultsByInspectionId(inspection.id)
        } else {
            flowOf(emptyList())
        }
        combine(pointsFlow, equipmentResultsFlow) { points, results ->
            val completedCount = results.count {
                it.status == LocalEquipmentResultStatus.COMPLETED
            }
            val hasPendingSync = inspection?.syncStatus == LocalSyncStatus.PENDING &&
                inspection.status in COMPLETED_INSPECTION_STATUSES
            val effectiveStatus = resolveEffectiveStatus(
                assignmentStatus = assignment.status,
                inspectionStatus = inspection?.status,
                totalPoints = points.size,
                completedCount = completedCount,
            )
            mapper.map(
                assignment = assignment,
                route = route,
                status = effectiveStatus,
                totalPoints = points.size,
                completedPoints = completedCount,
                inspectionId = inspection?.id,
                hasPendingSync = hasPendingSync,
            )
        }
    }
}
```

Удалить старый `getAssignments` блок (строки 35-75). Импорты:

```kotlin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
```

`flatMapLatest` требует `@OptIn(ExperimentalCoroutinesApi::class)` на классе или функции.

`routeStorage.selectRouteById(assignment.routeId)` остаётся suspend — он не реактивный (запись `routes` обычно меняется только при `applyConfigChanges`, не во время инспекции). Если detekt запрещает прямые suspend-вызовы из non-suspend контекста, оборачивать в `runBlocking` запрещено — заменить на `observeRouteById` (добавить в storage по аналогии с Step 2.2) или вытащить routes одним Flow вверх. На первой итерации оставляем `selectRouteById` как non-suspend — это `fun` в текущем `RouteStorage.kt` (строка 14), что нормально.

`startInspection` оставить как есть (suspend).

- [ ] **Step 3.3: Переписать `RoutesListExecutor.kt`**

Заменить файл:

```kotlin
package ru.mirea.toir.feature.routes.list.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Intent
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Label
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory.Message
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository

internal class RoutesListExecutor(
    private val repository: RoutesListRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Unit, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        repository.observeAssignments()
            .onStart { dispatch(Message.SetLoading) }
            .onEach { dispatch(Message.SetAssignments(it)) }
            .catch { dispatch(Message.SetError) }
            .launchIn(scope)
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.Refresh -> Unit
            is Intent.OnStartInspection -> startInspection(intent.assignmentId)
            is Intent.OnContinueInspection -> publish(
                Label.NavigateToRoutePoints(intent.inspectionId)
            )
        }
    }

    private suspend fun startInspection(assignmentId: String) {
        repository.startInspection(assignmentId).fold(
            onSuccess = { inspectionId ->
                publish(Label.NavigateToRoutePoints(inspectionId))
            },
            onFailure = { dispatch(Message.SetError) },
        )
    }
}
```

`Intent.Refresh` теперь no-op — Flow сам пушит обновления. Удалять Intent из контракта не нужно (UI всё ещё может его дёргать как hint, например для pull-to-refresh; сейчас он просто не делает работы). Если в `RoutesListStore` есть documentation у Intent — обновить комментарий, что pull-to-refresh теперь декоративный.

- [ ] **Step 3.4: Удалить временный `LifecycleEventEffect(ON_RESUME)` в `RoutesListScreen.kt`**

В файле `shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/RoutesListScreen.kt` удалить строки 43-45:

```kotlin
LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.onRefresh()
}
```

И удалить импорты, которые становятся неиспользуемыми:

```kotlin
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
```

- [ ] **Step 3.5: Обновить InMemoryRoutesListRepository, если есть**

```bash
find /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-routes-list -name "*InMemory*" -o -name "*Fake*"
```

Если файл найден — заменить `suspend fun getAssignments` на `fun observeAssignments(): Flow<…>` с внутренним `MutableStateFlow<List<DomainRouteAssignment>>` и `.asStateFlow()` getter. Если файла нет — пропустить шаг.

- [ ] **Step 3.6: Build + tests verification**

```bash
./gradlew detekt :shared:feature-routes-list:impl:allTests :android:app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Если `RoutesListReducerTest` падает — открыть, посмотреть какие Message-кейсы он покрывает; реальный fix: тесты на reducer не должны были зависеть от Executor/Repository, проблем быть не должно.

- [ ] **Step 3.7: Manual smoke test (ADB)**

Собрать и установить APK:

```bash
./gradlew :android:app:installDevDebug
```

На запущенном emulator-5556 проверить:
1. Залогиниться (operator01 / password).
2. На экране routes list нажать "Начать" на одном из assignment.
3. На экране точек нажать **system back** — вернуться на routes list.
4. Убедиться: статус assignment на routes list **сменился с ASSIGNED на IN_PROGRESS без явного refresh-gesture**. Это та же история, что чинил commit `b09f0ec`, но теперь — через Flow.

Если статус не меняется — проблема либо в `combine`-цепочке `RoutesListRepositoryImpl` (распечатать Napier в Executor `onEach`), либо в том, что write-операция `inspectionStorage.insertInspection` идёт мимо asFlow-уведомления (это означало бы баг в SQLDelight Impl — крайне маловероятно).

- [ ] **Step 3.8: Commit**

```bash
git add shared/feature-routes-list
git commit -m "$(cat <<'EOF'
refactor(routes-list): observe assignments via SQLDelight Flow

Replace suspend RoutesListRepository.getAssignments() with observeAssignments():
Flow<List<DomainRouteAssignment>> built from combine() of RouteStorage and
InspectionStorage observe-flows. RoutesListExecutor subscribes once in
suspendExecuteAction and uses onStart/onEach/catch to drive Loading/Loaded/Error
messages.

Remove LifecycleEventEffect(ON_RESUME) workaround from RoutesListScreen — Flow
now propagates updates from local DB writes (startInspection, sync push) directly.

Intent.Refresh is kept for API compatibility but is now a no-op.
EOF
)"
```

---

### Task 4: RoutePoints feature — переход на Flow

**Files:**
- Modify: `shared/feature-route-points/impl/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/impl/domain/repository/RoutePointsRepository.kt`
- Modify: `shared/feature-route-points/impl/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/impl/data/repository/RoutePointsRepositoryImpl.kt`
- Modify: `shared/feature-route-points/impl/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/impl/domain/RoutePointsExecutor.kt`

#### Шаги

- [ ] **Step 4.1: Прочитать текущий RoutePointsExecutor**

```bash
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-route-points/impl/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/impl/domain/RoutePointsExecutor.kt
```

Найти как сейчас `getRoutePoints(inspectionId)` вызывается из bootstrapper-action и какие Message используются. Сохранить имена Message-вариантов (например, `SetPoints`, `SetLoading`, `SetError`).

- [ ] **Step 4.2: Изменить контракт `RoutePointsRepository.kt`**

```kotlin
package ru.mirea.toir.feature.route.points.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

internal interface RoutePointsRepository {
    fun observeRoutePoints(inspectionId: String): Flow<Pair<String, List<DomainRoutePoint>>>
    suspend fun finishInspection(inspectionId: String): Result<Unit>
}
```

Первый элемент пары — `routeName`, второй — список точек с актуальным статусом (`UiEquipmentResultStatus`).

- [ ] **Step 4.3: Переписать `RoutePointsRepositoryImpl.kt`**

Прочитать существующий impl:

```bash
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-route-points/impl/src/commonMain/kotlin/ru/mirea/toir/feature/route/points/impl/data/repository/RoutePointsRepositoryImpl.kt
```

Заменить `getRoutePoints` на:

```kotlin
override fun observeRoutePoints(
    inspectionId: String,
): Flow<Pair<String, List<DomainRoutePoint>>> {
    val inspection = inspectionStorage.selectInspectionById(inspectionId)
        ?: return flowOf("" to emptyList())
    val route = routeStorage.selectRouteById(inspection.routeId)
    val routeName = route?.name.orEmpty()

    val pointsFlow = routeStorage.observePointsByRouteId(inspection.routeId)
    val equipmentResultsFlow = inspectionStorage.observeEquipmentResultsByInspectionId(inspectionId)

    return combine(pointsFlow, equipmentResultsFlow) { points, results ->
        val resultByPoint = results.associateBy { it.routePointId }
        val domainPoints = points.map { point ->
            val equipment = equipmentStorage.selectEquipmentById(point.equipmentId)
            val result = resultByPoint[point.id]
            mapper.map(
                point = point,
                equipment = equipment,
                equipmentResult = result,
            )
        }
        routeName to domainPoints
    }.flowOn(coroutineDispatchers.io)
}
```

(Имена mapper-функций сверить с тем, что сейчас в Impl — пакет `ru.mirea.toir.feature.route.points.impl.data.mappers`.)

`finishInspection` оставить как есть.

- [ ] **Step 4.4: Переписать `RoutePointsExecutor.kt`**

Bootstrapper action для RoutePoints передаёт `inspectionId` через parametersOf (см. memo 1160). Найти, как `inspectionId` поступает в Executor сейчас (либо через constructor, либо через `Action.Init(inspectionId)`). Заменить разовый запрос на подписку:

```kotlin
override suspend fun suspendExecuteAction(action: Action) {
    when (action) {
        is Action.Init -> observePoints(action.inspectionId)
    }
}

private fun observePoints(inspectionId: String) {
    repository.observeRoutePoints(inspectionId)
        .onStart { dispatch(Message.SetLoading) }
        .onEach { (routeName, points) ->
            dispatch(Message.SetPoints(routeName = routeName, points = points))
        }
        .catch { dispatch(Message.SetError) }
        .launchIn(scope)
}
```

Имя `Action.Init` и `Message.SetPoints` сверить с существующим контрактом.

- [ ] **Step 4.5: Обновить тесты RoutePoints, если есть**

```bash
find /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-route-points -path "*Test*" -name "*.kt"
```

Если в тестах есть `InMemoryRoutePointsRepository` или mocks возвращающие `Result.success(...)` — заменить на `flowOf(...)` подходящего типа.

- [ ] **Step 4.6: Build verification**

```bash
./gradlew detekt :shared:feature-route-points:impl:allTests :android:app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4.7: Manual smoke test**

После переустановки APK:
1. Открыть routes list → "Начать" → попасть на route points.
2. Нажать на одну точку → попасть на equipment card → пройти чек-лист до конца.
3. Вернуться (system back) на route points.
4. Убедиться: статус пройденной точки на route points **сменился на COMPLETED без refresh-gesture**.

- [ ] **Step 4.8: Commit**

```bash
git add shared/feature-route-points
git commit -m "$(cat <<'EOF'
refactor(route-points): observe route points via SQLDelight Flow

Replace RoutePointsRepository.getRoutePoints() with observeRoutePoints():
Flow<Pair<String, List<DomainRoutePoint>>> built from combine() of
route-points and equipment-results observe-flows. Returning to this screen
after an equipment inspection now reflects the new COMPLETED status
without any explicit reload.
EOF
)"
```

---

### Task 5: EquipmentCard feature — переход на Flow

**Files:**
- Modify: `shared/feature-equipment-card/impl/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/impl/domain/repository/EquipmentCardRepository.kt`
- Modify: `shared/feature-equipment-card/impl/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/impl/data/repository/EquipmentCardRepositoryImpl.kt`
- Modify: `shared/feature-equipment-card/impl/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/impl/domain/EquipmentCardExecutor.kt`

#### Шаги

- [ ] **Step 5.1: Прочитать существующий код**

```bash
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-equipment-card/impl/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/impl/domain/repository/EquipmentCardRepository.kt
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-equipment-card/impl/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/impl/domain/EquipmentCardExecutor.kt
```

Запомнить имена методов и Message.

- [ ] **Step 5.2: Изменить контракт `EquipmentCardRepository.kt`**

```kotlin
package ru.mirea.toir.feature.equipment.card.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

internal interface EquipmentCardRepository {
    fun observeEquipmentCard(
        inspectionId: String,
        routePointId: String,
    ): Flow<DomainEquipmentCard>

    suspend fun ensureEquipmentResult(
        inspectionId: String,
        routePointId: String,
    ): Result<String>
}
```

Точное имя `DomainEquipmentCard` сверить с api-моделями фичи. Если read и write сейчас слиты в один suspend-метод — разделить: read становится `observeEquipmentCard`, ensure/insert остаётся suspend.

- [ ] **Step 5.3: Реализовать `observeEquipmentCard` в Impl**

```kotlin
override fun observeEquipmentCard(
    inspectionId: String,
    routePointId: String,
): Flow<DomainEquipmentCard> {
    val routePoint = routeStorage.selectPointById(routePointId)
        ?: return flowOf(DomainEquipmentCard.empty())
    val equipmentFlow = equipmentStorage.observeEquipmentById(routePoint.equipmentId)
    val resultFlow = inspectionStorage.observeEquipmentResultByRoutePoint(
        routePointId = routePointId,
        inspectionId = inspectionId,
    )
    return combine(equipmentFlow, resultFlow) { equipment, result ->
        mapper.map(
            routePoint = routePoint,
            equipment = equipment,
            equipmentResult = result,
        )
    }.flowOn(coroutineDispatchers.io)
}
```

`DomainEquipmentCard.empty()` — добавить companion object фабрику; либо использовать nullable Flow и `filterNotNull()` в Executor.

- [ ] **Step 5.4: Переписать `EquipmentCardExecutor.kt`**

Аналогично Task 4.4 — подписка через `observeEquipmentCard(inspectionId, routePointId).onStart{Loading}.onEach{Loaded}.catch{Error}.launchIn(scope)`.

- [ ] **Step 5.5: Build + tests**

```bash
./gradlew detekt :shared:feature-equipment-card:impl:allTests :android:app:assembleDebug
```

- [ ] **Step 5.6: Manual smoke test**

1. Route points → нажать точку → equipment card.
2. Открыть чек-лист, пройти все пункты, нажать "Завершить".
3. Возврат на equipment card.
4. Убедиться: статус на equipment card отображается актуальный (COMPLETED) без refresh-gesture.

- [ ] **Step 5.7: Commit**

```bash
git add shared/feature-equipment-card
git commit -m "$(cat <<'EOF'
refactor(equipment-card): observe equipment card via SQLDelight Flow

Replace EquipmentCardRepository.getEquipmentCard() with observeEquipmentCard():
Flow<DomainEquipmentCard> built from equipment + equipment-result observe-flows.
Status after checklist completion is now reactive — no manual reload needed.
EOF
)"
```

---

### Task 6: Checklist feature — переход на Flow

**Files:**
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/repository/ChecklistRepository.kt`
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/data/repository/ChecklistRepositoryImpl.kt`
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/ChecklistExecutor.kt`

#### Шаги

- [ ] **Step 6.1: Прочитать существующий код**

```bash
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/repository/ChecklistRepository.kt
grep -n "" /Users/a.dobrov/StudioProjects/toir-mobile/shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/ChecklistExecutor.kt
```

Запомнить имена read- vs write-методов. **Только read** переводим на Flow; все `saveBooleanAnswer`/`saveNumberAnswer`/`saveTextAnswer`/`saveSelectAnswer`/`saveConfirm`/`addPhoto`/`finishChecklist` остаются suspend.

- [ ] **Step 6.2: Добавить `observeChecklist` в contract**

```kotlin
fun observeChecklist(equipmentResultId: String): Flow<DomainChecklist>
```

(остальные suspend write-методы — без изменений)

- [ ] **Step 6.3: Реализовать в Impl**

```kotlin
override fun observeChecklist(
    equipmentResultId: String,
): Flow<DomainChecklist> {
    val equipmentResult = inspectionStorage.selectEquipmentResultById(equipmentResultId)
        ?: return flowOf(DomainChecklist.empty())
    val routePoint = routeStorage.selectPointById(equipmentResult.routePointId)
        ?: return flowOf(DomainChecklist.empty())

    val itemsFlow = checklistStorage.observeItemsByChecklistId(routePoint.checklistId)
    val itemResultsFlow = inspectionStorage.observeChecklistItemResultsByEquipmentResult(
        equipmentResultId = equipmentResultId,
    )

    return combine(itemsFlow, itemResultsFlow) { items, results ->
        val resultByItemId = results.associateBy { it.checklistItemId }
        mapper.map(
            items = items,
            answersByItemId = resultByItemId,
        )
    }.flowOn(coroutineDispatchers.io)
}
```

- [ ] **Step 6.4: Переписать `ChecklistExecutor.kt`**

Read-подписка через Flow; write-handlers (sаveBooleanAnswer и т.д.) оставить как есть — Executor через write дёргает suspend, write попадает в SQLDelight, asFlow доставляет обратно в read-подписку → reducer пересчитывает state.

```kotlin
override suspend fun suspendExecuteAction(action: Action) {
    when (action) {
        is Action.Init -> {
            repository.observeChecklist(action.equipmentResultId)
                .onStart { dispatch(Message.SetLoading) }
                .onEach { dispatch(Message.SetChecklist(it)) }
                .catch { dispatch(Message.SetError) }
                .launchIn(scope)
        }
    }
}
```

(Точное имя Action.Init / Message.* сверить.)

- [ ] **Step 6.5: Build + tests**

```bash
./gradlew detekt :shared:feature-checklist:impl:allTests :android:app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Existing checklist reducer tests должны продолжать проходить — мы не меняем reducer.

- [ ] **Step 6.6: Manual smoke test**

1. Открыть чек-лист.
2. Ответить на один вопрос (например, переключить Boolean).
3. Убедиться: UI **сразу отражает изменение** — это всегда работало, потому что StateFlow от Store. Главное — что после `onConfirm` или `addPhoto` не остаётся stale ChecklistItem'а.
4. Нажать "Сохранить и продолжить" — успех.

- [ ] **Step 6.7: Commit**

```bash
git add shared/feature-checklist
git commit -m "$(cat <<'EOF'
refactor(checklist): observe checklist state via SQLDelight Flow

Replace ChecklistRepository.getChecklist() with observeChecklist():
Flow<DomainChecklist> built from checklist items + answer-results observe-flows.
Writes (save*Answer, addPhoto, finishChecklist) remain suspend; they update
SQLDelight which the read-Flow picks up automatically.
EOF
)"
```

---

### Task 7: Очистка memory и финальная верификация

**Files:**
- Modify: `/Users/a.dobrov/.claude/projects/-Users-a-dobrov-StudioProjects-toir-mobile/memory/feedback_reactivity_whileSubscribed.md`

#### Шаги

- [ ] **Step 7.1: Обновить заметку в memory**

В файле `feedback_reactivity_whileSubscribed.md` удалить блок:

> Уже добавленный фикс в RoutesListScreen (LifecycleEventEffect ON_RESUME → onRefresh) считать временным — переписать на Flow-проекцию, как только репозиторий выставит Flow.

И блок "Точечно — куда применять прямо сейчас". Сохранить только общее правило: "Реактивность на Flow из SQLDelight; ON_RESUME refresh — только для http-источников".

- [ ] **Step 7.2: Финальный smoke run полного флоу**

End-to-end сценарий на emulator:
1. Login → Routes list. Список загружается без видимого "перепрыга" loading-state'ом (Flow `onStart` сработает один раз).
2. "Начать" по assignment → переход на Route points.
3. Нажать system back → возврат на Routes list, статус assignment моментально **IN_PROGRESS**.
4. Снова открыть → выбрать первую точку → Equipment card.
5. Открыть чек-лист → пройти все пункты → "Завершить".
6. Возврат на Equipment card → статус **COMPLETED**.
7. Возврат на Route points → точка отображается как **COMPLETED**.
8. Возврат на Routes list → completedPoints счётчик обновился.

Любая stale-статусность = регрессия.

- [ ] **Step 7.3: Финальный build**

```bash
./gradlew detekt :android:app:assembleDebug
./gradlew :shared:feature-routes-list:impl:allTests :shared:feature-route-points:impl:allTests :shared:feature-equipment-card:impl:allTests :shared:feature-checklist:impl:allTests
```

Expected: `BUILD SUCCESSFUL` везде.

- [ ] **Step 7.4: Создать PR**

```bash
git push -u origin feature/reactivity-flow-migration
gh pr create --title "feat(reactivity): observe-flow migration + RoutePoints back button" --body "$(cat <<'EOF'
## Summary

- Add back button to RoutePointsScreen (onNavigateBack callback through API/Screen/TopBar layers).
- Migrate 4 feature repositories (routes-list, route-points, equipment-card, checklist) from suspend `getX(): Result<List<…>>` to `observeX(): Flow<…>` built on SQLDelight `asFlow().mapToList(io)`.
- Remove temporary `LifecycleEventEffect(ON_RESUME) { onRefresh() }` workaround from RoutesListScreen — Flow now propagates DB writes (startInspection, equipment-result writes, checklist writes, sync push) reactively.

## Architecture

- Each `StorageImpl` now takes `CoroutineDispatchers` in its constructor to pass `io` dispatcher to `mapToList`.
- Projection logic (e.g. `resolveEffectiveStatus`) moved inside `combine()` of N storage flows.
- Subscription lives in Executor `scope` (= NavBackStackEntry lifetime), semantically equivalent to WhileSubscribed for our MVIKotlin layout (no `stateIn` between Store and UI).
- `Intent.Refresh` on routes-list is kept for API compatibility but is now a no-op.

## Test plan

- [x] Detekt clean
- [x] `:android:app:assembleDebug` builds
- [x] All feature `:impl:allTests` pass
- [x] Manual emulator smoke test of full flow (login → routes → points → equipment → checklist → back) — every status change visible without refresh-gesture
EOF
)"
```

---

## Self-Review

**Spec coverage:**
- Back button RoutePoints → Task 1 ✅
- Реактивность routes-list → Task 3 (включая удаление ON_RESUME) ✅
- Реактивность route-points → Task 4 ✅
- Реактивность equipment-card → Task 5 ✅
- Реактивность checklist → Task 6 ✅
- WhileSubscribed(5_000) семантика → объяснено в Architecture (Executor scope = NavBackStackEntry lifetime) ✅
- onStart/onEach/catch в Executor → используется во всех таскax 3-6 ✅
- Один PR на всё → Task 7.4 ✅

**Type consistency:** имена контрактов consistent — `observeAssignments`, `observeRoutePoints`, `observeEquipmentCard`, `observeChecklist`, все возвращают `Flow<...>`. Storage `observe*` методы тоже единообразны.

**Placeholder scan:** в Task 4.4 / 5.4 / 6.4 указано "Имена `Action.Init` / `Message.*` сверить с существующим контрактом" — это не плейсхолдер, а явная инструкция читать существующий код одной командой `grep -n` перед правкой. Названия Message в RoutesList указаны точно (`SetLoading`/`SetError`/`SetAssignments` — взяты из существующего `RoutesListStoreFactory.kt:31-33`).
