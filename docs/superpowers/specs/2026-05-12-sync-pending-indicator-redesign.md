# Sync Pending Indicator — Redesign

**Дата:** 2026-05-12
**Статус:** Design ready
**Связан с:** `docs/superpowers/plans/10-sync-completion.md` (Phase 3 follow-up)

## Контекст

Текущий sync-индикатор показывает в бэйдже **число** «Ожидает отправки», которое суммирует `count(*) WHERE sync_status='pending'` по пяти таблицам: `inspections`, `inspection_equipment_results`, `checklist_item_results`, `photos`, `action_logs`.

Проблемы:

1. **Гранулярность не соответствует ментальной модели.** Один обход порождает десятки строк (показания, чек-листы, фото). Юзер видит «14» и не понимает, к чему это число относится. «1 обход → 14 ожидает» — выглядит как баг.
2. **Состав смешан.** В сумму входят `action_logs` — телеметрия, не пользовательские данные.
3. **Per-inspection причина rejection невидима.** Бэк уже умеет partial-accept: `RemoteSyncPushResponse.rejected: List<RemoteSyncRejected>` с типизированной бизнес-причиной (`RemoteSyncRejectedReason`: 8 значений). Сейчас эта причина пишется в `inspection.sync_last_error` строкой и до UI не доходит. Юзер не понимает, почему конкретный обход не уходит.
4. **Смешение семантик в одной колонке.** `sync_last_error` пишется И при transport-fail (`SyncFailureReason.name` — NETWORK/AUTH/SERVER/UNKNOWN), И при bizz-rejection (`RemoteSyncRejectedReason.name` — 8 значений). Одна строковая колонка, две разные категории — антипаттерн.

## Цели

- Убрать число из бэйджа, заменить статусным индикатором.
- В bottom sheet показывать **список обходов**, ждущих отправки, с понятной причиной для каждого rejected.
- Типизировать persistance: отдельная колонка под бизнес-rejection через `ColumnAdapter`.
- Передавать причину по типизированной лесенке от БД до UI, без runtime-парсинга строк.

## Не-цели

- Изменение retry-логики (zombie-обходы, бесконечный retry на bizz-rejection) — known issue, отдельный план.
- Введение `sync_status = 'rejected'` — отдельный план.
- Изменения серверного API.

## Принципы

1. Один столбец — одна семантика.
2. Типизация на границах слоёв через явные mappers. Никакого `valueOf` / `runCatching` в логике.
3. Persistance — typed через `ColumnAdapter` (стандарт SQLDelight, уже используется для `LocalSyncStatus`/`LocalInspectionStatus`).
4. Стрелка зависимостей: `database → domain → feature.api → presentation → ui`. Database-local enum'ы не утекают наверх.

## Схема БД

> **Миграции не пишутся** — проект в активной разработке, БД пересоздаётся между билдами.

В `Inspection.sq` (и аналогично `InspectionEquipmentResult.sq`, `ChecklistItemResult.sq`):

```diff
- sync_last_error TEXT
+ sync_rejection_reason TEXT AS LocalRejectionReason
```

`LocalRejectionReason` — новый enum в `shared/core-database/.../models/LocalRejectionReason.kt` с 8 значениями (зеркало `RemoteSyncRejectedReason`):

```kotlin
enum class LocalRejectionReason(override val localValue: String) : LocalEnum {
    INVALID_ASSIGNMENT_ID("invalid_assignment_id"),
    INVALID_ROUTE_ID("invalid_route_id"),
    ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN("route_assignment_not_found_or_forbidden"),
    ROUTE_ID_MISMATCH("route_id_mismatch"),
    INSPECTION_NOT_FOUND("inspection_not_found"),
    ROUTE_POINT_NOT_FOUND("route_point_not_found"),
    EQUIPMENT_MISMATCH("equipment_mismatch"),
    UNKNOWN("unknown"),
}
```

`EnumColumnAdapter` регистрируется в DI core-database вместе с существующими адаптерами.

## SQLDelight: новые запросы

```sql
-- Inspection.sq
selectHasPending:
SELECT
    EXISTS(SELECT 1 FROM inspections WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM inspection_equipment_results WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM checklist_item_results WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM photos WHERE sync_status = 'pending');

selectPendingInspections:
SELECT i.id, i.assignment_id, i.route_id, i.status, i.completed_at,
       i.sync_attempt_count, i.sync_rejection_reason
FROM inspections i
WHERE i.status IN ('completed', 'partially_completed', 'cancelled')
  AND (
    i.sync_status = 'pending'
    OR EXISTS (SELECT 1 FROM inspection_equipment_results
               WHERE inspection_id = i.id AND sync_status = 'pending')
    OR EXISTS (SELECT 1 FROM checklist_item_results cir
               JOIN inspection_equipment_results ier ON cir.inspection_equipment_result_id = ier.id
               WHERE ier.inspection_id = i.id AND cir.sync_status = 'pending')
    OR EXISTS (SELECT 1 FROM photos p
               JOIN checklist_item_results cir ON p.checklist_item_result_id = cir.id
               JOIN inspection_equipment_results ier ON cir.inspection_equipment_result_id = ier.id
               WHERE ier.inspection_id = i.id AND p.sync_status = 'pending')
  )
ORDER BY i.completed_at DESC;
```

`action_logs` намеренно исключены из `selectHasPending` — это телеметрия, на индикатор статуса не влияет.

Оба Flow'а реактивны через `Query.asFlow()` — переэмитят при изменении любой из перечисленных таблиц.

## Storage API

В `InspectionStorage`:

```kotlin
fun observeHasPending(): Flow<Boolean>
fun observePendingInspections(): Flow<List<LocalPendingInspection>>
fun markRejected(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
    reason: LocalRejectionReason,
)
```

`markInspectionSynced` дополнительно обнуляет `sync_rejection_reason = NULL`.
`markInspectionRetryScheduled` (transport fail) — НЕ пишет `sync_rejection_reason`.

`LocalPendingInspection` — `data class` в database-слое, выходной DTO storage'а:

```kotlin
data class LocalPendingInspection(
    val id: String,
    val assignmentId: String?,
    val routeId: String,
    val status: LocalInspectionStatus,
    val completedAt: String?,
    val attemptCount: Long,
    val rejectionReason: LocalRejectionReason?,
)
```

## Domain (sync-manager)

```kotlin
// sync-manager/domain
data class DomainPendingInspection(
    val inspectionId: String,
    val routeId: String,
    val assignmentId: String?,
    val completedAt: Instant?,
    val status: PendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: InspectionRejectionReason?,
)

enum class PendingInspectionStatus { COMPLETED, PARTIALLY_COMPLETED, CANCELLED }
enum class InspectionRejectionReason {
    INVALID_ASSIGNMENT_ID,
    INVALID_ROUTE_ID,
    ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
    ROUTE_ID_MISMATCH,
    INSPECTION_NOT_FOUND,
    ROUTE_POINT_NOT_FOUND,
    EQUIPMENT_MISMATCH,
    UNKNOWN,
}
```

`SyncRepository` / `SyncManager`:

```diff
- val pendingCount: Flow<Long>
- fun observePendingCount(): Flow<Long>
+ val hasPending: Flow<Boolean>
+ val pendingInspections: Flow<List<DomainPendingInspection>>
+ fun observeHasPending(): Flow<Boolean>
+ fun observePendingInspections(): Flow<List<DomainPendingInspection>>
```

## Точка записи в SyncRepositoryImpl

- `scheduleBatchRetry` (transport fail) — пишет **только** `attemptCount` и `nextAttemptAt` через `markInspectionRetryScheduled`. Без rejection reason.
- `handleRejected` — мапит `RemoteSyncRejectedReason → LocalRejectionReason` и вызывает `markRejected(...)`.
- Маппер `RemoteSyncRejectedReason → LocalRejectionReason` живёт в `sync-manager/data/mappers`, 1-к-1.

## Feature.api (routes-list)

```kotlin
// feature-routes-list/api
data class RoutesListPendingInspection(
    val inspectionId: String,
    val routeName: String?,
    val completedAt: Instant?,
    val status: RoutesListPendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: RoutesListRejectionReason?,
)

enum class RoutesListPendingInspectionStatus { COMPLETED, PARTIALLY_COMPLETED, CANCELLED }
enum class RoutesListRejectionReason { /* 8 значений */ }
```

`RoutesListSyncIndicator`:

```diff
data class RoutesListSyncIndicator(
-   val pendingCount: Int,
+   val hasPending: Boolean,
+   val pendingInspections: List<RoutesListPendingInspection>,
    val lastError: RoutesListSyncFailure?,
    val isRunning: Boolean,
    val lastSuccessAt: Instant?,
)
```

В `RoutesListRepositoryImpl`:
- combine `syncManager.hasPending`, `syncManager.pendingInspections`, плюс существующие sync-meta флоу
- enrichment `routeName` через JOIN с локальной таблицей `routes` (отдельный storage запрос — sync-manager про routes не знает)
- mapper `DomainPendingInspection → RoutesListPendingInspection`

## Presentation

```kotlin
// presentation/models
data class UiPendingInspection(
    val inspectionId: String,
    val routeName: String?,
    val completedAt: Instant?,
    val status: UiPendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: UiRejectionReason?,
)

enum class UiPendingInspectionStatus { COMPLETED, PARTIALLY_COMPLETED, CANCELLED }
enum class UiRejectionReason { /* 8 значений */ }
```

`UiSyncIndicator`:

```diff
- val pendingCount: Int
+ val hasPending: Boolean
+ val pendingInspections: List<UiPendingInspection>
```

`UiRoutesListStateMapper` — расширить маппинг.

## UI

### SyncIndicatorIcon

Убрать `Badge { Text(pendingCount) }`. Состояния остаются те же, но без бэйджа:

| Условие | Иконка | Цвет |
|---|---|---|
| `isRunning` | анимированный sync | accent |
| `lastError != null` | `ic_sync_off` | error |
| `hasPending` | `ic_sync_alt` | warning |
| иначе | `ic_sync` | ok |

### SyncStatusBottomSheet

Удалить `PendingCountCard`. Добавить секцию «Обходы ждут отправки»:

- `pendingInspections.isEmpty() && !hasPending` → текст «Всё синхронизировано»
- `pendingInspections.isEmpty() && hasPending` → текст «Данные отправляются в фоне»
- иначе — `LazyColumn` карточек:
  - **рисуем строку:** название маршрута (или fallback по `inspectionId`), время завершения, статус (COMPLETED / PARTIALLY_COMPLETED / CANCELLED)
  - `rejectionReason != null` → красная пометка «**Обход отвергнут**: *<recipe из MR.strings.sync_rejection_X>*»
  - `attemptCount > 0 && rejectionReason == null` → серая пометка «попыток: N»

### Строковые ресурсы (MR.strings)

Добавить:
- `sync_status_pending_list_title` — «Ожидают отправки»
- `sync_status_pending_list_empty` — «Все обходы синхронизированы»
- `sync_status_pending_in_background` — «Данные отправляются в фоне»
- `sync_status_pending_attempts` — «попыток: %d»
- `sync_status_pending_rejected_prefix` — «Обход отвергнут: »
- `sync_rejection_invalid_assignment_id` — «недопустимое задание»
- `sync_rejection_invalid_route_id` — «недопустимый маршрут»
- `sync_rejection_route_assignment_not_found` — «задание не найдено или нет доступа»
- `sync_rejection_route_id_mismatch` — «несоответствие маршрута»
- `sync_rejection_inspection_not_found` — «обход не найден»
- `sync_rejection_route_point_not_found` — «точка маршрута не найдена»
- `sync_rejection_equipment_mismatch` — «несоответствие оборудования»
- `sync_rejection_unknown` — «неизвестная причина»

Удалить (больше не используются):
- `sync_status_pending_label`
- `sync_status_pending_zero`

## Цепочка типов (резюме)

```
RemoteSyncRejectedReason   (network DTO)
        ↓ mapper
LocalRejectionReason       (database, ColumnAdapter)
        ↓ mapper
InspectionRejectionReason  (sync-manager.domain)
        ↓ mapper
RoutesListRejectionReason  (routes-list.api)
        ↓ mapper
UiRejectionReason          (presentation)
        ↓ toMessageRes()
MR.strings.sync_rejection_*  (ui)
```

Симметрично существующей цепочке `SyncFailureReason → RoutesListSyncFailure → UiSyncFailure → MR.strings.sync_error_*`.

## Тесты

- `RoutesListReducerTest` — обновить три кейса с `pendingCount: Int` → `hasPending: Boolean`, добавить два кейса на `pendingInspections` (пустой / с rejection / с attempts).
- Новый юнит-тест на маппер `DomainPendingInspection → RoutesListPendingInspection`.
- Новый юнит-тест на маппер `RemoteSyncRejectedReason → LocalRejectionReason`.
- (Если есть инфра in-memory SQLDelight) интеграционный тест `selectPendingInspections` для трёх кейсов: только inspection pending, только photo pending, ничего pending.

## Known issues (вне scope)

1. **Zombie-обходы при bizz-rejection.** Сейчас `handleRejected` вызывает `scheduleRetry` для отвергнутой записи — она будет вечно крутиться в retry-цикле с одной и той же причиной. Нужен отдельный sync_status `'rejected'` (вне retry-цикла) и UI action «удалить локально / связаться с мастером». Этот план показывает причину, но не разрывает retry-цикл. Следующая итерация.
2. **`action_logs`** — телеметрия, на UI индикатор не влияет. Если в будущем понадобится диагностика «логи тонут» — отдельная админская поверхность.

## Файлы, которые меняются

**Создаются:**
- `shared/core-database/.../models/LocalRejectionReason.kt`
- `shared/core-database/.../storage/inspection/models/LocalPendingInspection.kt`
- `shared/sync-manager/.../domain/DomainPendingInspection.kt`
- `shared/sync-manager/.../domain/InspectionRejectionReason.kt`
- `shared/sync-manager/.../domain/PendingInspectionStatus.kt`
- `shared/sync-manager/.../data/mappers/RemoteRejectedReasonMapper.kt`
- `shared/sync-manager/.../data/mappers/LocalRejectionReasonMapper.kt`
- `shared/sync-manager/.../data/mappers/PendingInspectionMapper.kt`
- `shared/feature-routes-list/api/.../models/RoutesListPendingInspection.kt`
- `shared/feature-routes-list/api/.../models/RoutesListRejectionReason.kt`
- `shared/feature-routes-list/presentation/.../models/UiPendingInspection.kt`
- `shared/feature-routes-list/presentation/.../models/UiRejectionReason.kt`

**Изменяются:**
- `shared/core-database/.../sqldelight/.../Inspection.sq` (схема + новые запросы)
- `shared/core-database/.../sqldelight/.../InspectionEquipmentResult.sq` (схема)
- `shared/core-database/.../sqldelight/.../ChecklistItemResult.sq` (схема)
- `shared/core-database/.../di/CoreDatabaseModule.kt` (регистрация ColumnAdapter)
- `shared/core-database/.../storage/inspection/InspectionStorage.kt` (+ методы)
- `shared/core-database/.../storage/inspection/InspectionStorageImpl.kt`
- `shared/sync-manager/.../domain/SyncManager.kt` (`pendingCount` → `hasPending`/`pendingInspections`)
- `shared/sync-manager/.../domain/repository/SyncRepository.kt`
- `shared/sync-manager/.../data/repository/SyncRepositoryImpl.kt` (`handleRejected`, `scheduleBatchRetry`, новые observe-методы)
- `shared/feature-routes-list/api/.../models/RoutesListSyncIndicator.kt`
- `shared/feature-routes-list/impl/.../data/repository/RoutesListRepositoryImpl.kt` (combine, mappers, JOIN с routes)
- `shared/feature-routes-list/impl/.../domain/RoutesListReducer.kt` (если нужно — смотрим в коде)
- `shared/feature-routes-list/presentation/.../models/UiRoutesListState.kt`
- `shared/feature-routes-list/presentation/.../mappers/UiRoutesListStateMapper.kt`
- `shared/feature-routes-list/ui/.../components/SyncIndicatorIcon.kt` (убрать бэйдж)
- `shared/feature-routes-list/ui/.../components/SyncStatusBottomSheet.kt` (удалить `PendingCountCard`, добавить список)
- `shared/feature-routes-list/impl/.../commonTest/.../RoutesListReducerTest.kt`
- `shared/core-common-ui/.../resources/strings.xml` (или где живут MR.strings) — добавить/удалить ресурсы
