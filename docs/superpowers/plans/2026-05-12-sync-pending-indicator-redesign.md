# Sync Pending Indicator Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Заменить числовой счётчик «Ожидает отправки» статусным индикатором + списком обходов с понятной причиной отказа от сервера.

**Architecture:** Колонка `inspection.sync_last_error` (смешанная семантика) удаляется. Per-inspection бизнес-причины rejection хранятся в новой типизированной колонке `sync_rejection_reason` через SQLDelight `ColumnAdapter`. Причина прокидывается по типизированной лесенке `LocalRejectionReason → InspectionRejectionReason → RoutesListRejectionReason → UiRejectionReason → MR.strings`, симметрично существующей `SyncFailureReason`-цепочке.

**Tech Stack:** Kotlin Multiplatform, SQLDelight (EnumColumnAdapter), Koin, MVIKotlin (Reducer/Store), Jetpack Compose Multiplatform, moko-resources.

**Связан со spec:** `docs/superpowers/specs/2026-05-12-sync-pending-indicator-redesign.md`

**Note по миграциям:** проект в активной разработке, БД пересоздаётся между билдами. Schema migration (`.sqm`) не пишем — разработчик при необходимости делает `adb shell pm clear` / переустанавливает приложение.

---

## File Structure

**Создаются:**
- `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/models/LocalRejectionReason.kt`
- `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/models/LocalPendingInspection.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/domain/DomainPendingInspection.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/domain/InspectionRejectionReason.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/domain/PendingInspectionStatus.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/mappers/RemoteRejectedReasonMapper.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/mappers/LocalRejectionReasonMapper.kt`
- `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/mappers/PendingInspectionMapper.kt`
- `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/mappers/RemoteRejectedReasonMapperTest.kt`
- `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/mappers/LocalRejectionReasonMapperTest.kt`
- `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/mappers/PendingInspectionMapperTest.kt`
- `shared/feature-routes-list/api/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/api/models/RoutesListPendingInspection.kt`
- `shared/feature-routes-list/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/presentation/models/UiPendingInspection.kt`
- `shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/components/PendingInspectionsSection.kt`

**Изменяются:**
- `shared/core-database/.../sqldelight/.../Inspection.sq`
- `shared/core-database/.../sqldelight/.../InspectionEquipmentResult.sq`
- `shared/core-database/.../sqldelight/.../ChecklistItemResult.sq`
- `shared/core-database/.../di/` (регистрация `EnumColumnAdapter`)
- `shared/core-database/.../storage/inspection/InspectionStorage.kt`
- `shared/core-database/.../storage/inspection/InspectionStorageImpl.kt`
- `shared/sync-manager/.../domain/SyncManager.kt`
- `shared/sync-manager/.../domain/repository/SyncRepository.kt`
- `shared/sync-manager/.../data/repository/SyncRepositoryImpl.kt`
- `shared/feature-routes-list/api/.../models/RoutesListSyncIndicator.kt`
- `shared/feature-routes-list/api/.../store/RoutesListStore.kt`
- `shared/feature-routes-list/impl/.../domain/RoutesListReducer.kt`
- `shared/feature-routes-list/impl/.../domain/RoutesListStoreFactory.kt`
- `shared/feature-routes-list/impl/.../data/repository/RoutesListRepositoryImpl.kt`
- `shared/feature-routes-list/impl/src/commonTest/.../domain/RoutesListReducerTest.kt`
- `shared/feature-routes-list/presentation/.../models/UiRoutesListState.kt`
- `shared/feature-routes-list/presentation/.../mappers/UiRoutesListStateMapper.kt`
- `shared/feature-routes-list/ui/.../components/SyncIndicatorIcon.kt`
- `shared/feature-routes-list/ui/.../components/SyncStatusBottomSheet.kt`
- Файл строковых ресурсов (находится по `MR.strings.sync_status_pending_label` через `grep -r sync_status_pending_label shared/`)

---

### Task 1: LocalRejectionReason enum

**Files:**
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/models/LocalRejectionReason.kt`

> **Note:** ColumnAdapter registration NOT in this task — it moved to Task 2. Reason: SQLDelight only generates an `*Adapter` parameter for an enum-typed column once that column exists in the schema. Until Task 2 changes `Inspection.sq` to `sync_rejection_reason TEXT AS LocalRejectionReason`, there's no adapter to register.

- [ ] **Step 1: Создать enum `LocalRejectionReason`**

```kotlin
// shared/core-database/.../models/LocalRejectionReason.kt
package ru.mirea.toir.core.database.models

enum class LocalRejectionReason(
    override val localValue: String,
) : LocalEnum {
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

- [ ] **Step 2: Build**

```bash
./gradlew :shared:core-database:compileKotlinMetadata
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/models/LocalRejectionReason.kt
git commit -m "feat(db): add LocalRejectionReason enum"
```

---

### Task 2: Schema changes — Inspection.sq + дочерние

**Files:**
- Modify: `shared/core-database/src/commonMain/sqldelight/ru/mirea/toir/core/database/Inspection.sq`
- Modify: `shared/core-database/src/commonMain/sqldelight/ru/mirea/toir/core/database/InspectionEquipmentResult.sq`
- Modify: `shared/core-database/src/commonMain/sqldelight/ru/mirea/toir/core/database/ChecklistItemResult.sq`

- [ ] **Step 1: Inspection.sq — заменить sync_last_error**

В `CREATE TABLE inspections`:
```diff
- sync_last_error TEXT
+ sync_rejection_reason TEXT AS LocalRejectionReason
```

Импорт сверху:
```sql
import ru.mirea.toir.core.database.models.LocalRejectionReason;
```

- [ ] **Step 2: Inspection.sq — обновить markRetryScheduled**

```sql
markRetryScheduled:
UPDATE inspections
SET sync_attempt_count = :attemptCount,
    sync_next_attempt_at = :nextAt
WHERE id = :id;
```

(параметр `sync_last_error` уходит).

- [ ] **Step 3: Inspection.sq — добавить markRejected**

```sql
markRejected:
UPDATE inspections
SET sync_attempt_count = :attemptCount,
    sync_next_attempt_at = :nextAt,
    sync_rejection_reason = :reason
WHERE id = :id;
```

- [ ] **Step 4: Inspection.sq — markSynced обнуляет rejection_reason**

```sql
markSynced:
UPDATE inspections
SET sync_status = 'synced',
    sync_attempt_count = 0,
    sync_next_attempt_at = NULL,
    sync_rejection_reason = NULL
WHERE id = :id;
```

- [ ] **Step 5: Inspection.sq — добавить selectHasPending**

```sql
selectHasPending:
SELECT (
    EXISTS(SELECT 1 FROM inspections WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM inspection_equipment_results WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM checklist_item_results WHERE sync_status = 'pending')
    OR EXISTS(SELECT 1 FROM photos WHERE sync_status = 'pending')
);
```

- [ ] **Step 6: Inspection.sq — добавить selectPendingInspections**

```sql
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

- [ ] **Step 7: InspectionEquipmentResult.sq — обновить колонку и retry**

```diff
- sync_last_error TEXT
+ sync_rejection_reason TEXT AS LocalRejectionReason
```

```sql
markRetryScheduled:
UPDATE inspection_equipment_results
SET sync_attempt_count = :attemptCount,
    sync_next_attempt_at = :nextAt
WHERE id = :id;

markRejected:
UPDATE inspection_equipment_results
SET sync_attempt_count = :attemptCount,
    sync_next_attempt_at = :nextAt,
    sync_rejection_reason = :reason
WHERE id = :id;
```

`markSynced` — добавить `sync_rejection_reason = NULL` (аналогично Inspection).

- [ ] **Step 8: ChecklistItemResult.sq — то же**

Структурно идентично InspectionEquipmentResult.sq.

- [ ] **Step 9: ActionLog.sq, Photo.sq — НЕ трогаем**

В action_logs и photos колонка `sync_last_error` остаётся как есть (мы туда per-entity reason не пишем, согласно spec).

- [ ] **Step 10: Запустить генерацию SQLDelight**

```bash
cd /Users/a.dobrov/StudioProjects/toir-mobile
./gradlew :shared:core-database:generateCommonMainToirDatabaseInterface
```

После этого сгенерированный `Inspections.Adapter` потребует параметр `sync_rejection_reasonAdapter`. Билд `compileKotlinMetadata` упадёт пока адаптер не зарегистрирован — это нормально, регистрируем на следующем шаге.

- [ ] **Step 11: Зарегистрировать LocalRejectionReason ColumnAdapter**

Открыть `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/di/CoreDatabaseModule.kt`.

В импортах рядом с `LocalSyncStatus` / `LocalInspectionStatus` добавить:
```kotlin
import ru.mirea.toir.core.database.models.LocalRejectionReason
```

В блоке `single<ToirDatabase> { ToirDatabase(...) }` дополнить `inspectionsAdapter = Inspections.Adapter(...)`:
```kotlin
inspectionsAdapter = Inspections.Adapter(
    statusAdapter = EnumColumnAdapter.create<LocalInspectionStatus>(),
    sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
    sync_rejection_reasonAdapter = EnumColumnAdapter.create<LocalRejectionReason>(),
),
```

Аналогично добавить `sync_rejection_reasonAdapter` в `inspection_equipment_resultsAdapter` и `checklist_item_resultsAdapter` (точные имена параметров — те, что сгенерированы SQLDelight после Step 10).

- [ ] **Step 12: Сборка**

```bash
./gradlew :shared:core-database:compileKotlinMetadata
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 13: Commit**

```bash
git add shared/core-database/
git commit -m "feat(db): replace sync_last_error with typed sync_rejection_reason"
```

---

### Task 3: InspectionStorage — добавить методы

**Files:**
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/InspectionStorage.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/models/LocalPendingInspection.kt`
- Modify: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/storage/inspection/InspectionStorageImpl.kt`

- [ ] **Step 1: Создать LocalPendingInspection**

```kotlin
// .../storage/inspection/models/LocalPendingInspection.kt
package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason

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

- [ ] **Step 2: Расширить интерфейс InspectionStorage**

Добавить:
```kotlin
fun observeHasPending(): Flow<Boolean>
fun observePendingInspections(): Flow<List<LocalPendingInspection>>

fun markInspectionRejected(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
    reason: LocalRejectionReason,
)
fun markEquipmentResultRejected(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
    reason: LocalRejectionReason,
)
fun markChecklistItemResultRejected(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
    reason: LocalRejectionReason,
)
```

Изменить существующие `markInspectionRetryScheduled` / `markEquipmentResultRetryScheduled` / `markChecklistItemResultRetryScheduled` — **убрать параметр `lastError`** (пишем только attemptCount + nextAttemptAt).

Удалить методы `observeInspectionPendingCount`, `observeEquipmentResultPendingCount`, `observeChecklistItemResultPendingCount` — больше не используются.

- [ ] **Step 3: Реализовать в InspectionStorageImpl**

```kotlin
override fun observeHasPending(): Flow<Boolean> =
    inspectionQueries.selectHasPending()
        .asFlow()
        .mapToOne(coroutineDispatchers.io)

override fun observePendingInspections(): Flow<List<LocalPendingInspection>> =
    inspectionQueries.selectPendingInspections()
        .asFlow()
        .mapToList(coroutineDispatchers.io)
        .map { rows ->
            rows.map { row ->
                LocalPendingInspection(
                    id = row.id,
                    assignmentId = row.assignment_id,
                    routeId = row.route_id,
                    status = row.status,
                    completedAt = row.completed_at,
                    attemptCount = row.sync_attempt_count,
                    rejectionReason = row.sync_rejection_reason,
                )
            }
        }

override fun markInspectionRejected(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
    reason: LocalRejectionReason,
) {
    inspectionQueries.markRejected(
        attemptCount = attemptCount,
        nextAt = nextAttemptAt,
        reason = reason,
        id = id,
    )
}
// аналогично для EquipmentResult / ChecklistItemResult
```

`markInspectionRetryScheduled` обновить — параметр `lastError` уходит, тело становится:
```kotlin
override fun markInspectionRetryScheduled(
    id: String,
    attemptCount: Long,
    nextAttemptAt: String,
) {
    inspectionQueries.markRetryScheduled(
        attemptCount = attemptCount,
        nextAt = nextAttemptAt,
        id = id,
    )
}
```

- [ ] **Step 4: Сборка core-database**

```bash
./gradlew :shared:core-database:compileKotlinMetadata
```
Ожидается: PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/core-database/
git commit -m "feat(db): expose hasPending and pendingInspections flows in InspectionStorage"
```

---

### Task 4: Domain модели в sync-manager (TDD-able через mapper-тесты)

**Files:**
- Create: `shared/sync-manager/.../domain/PendingInspectionStatus.kt`
- Create: `shared/sync-manager/.../domain/InspectionRejectionReason.kt`
- Create: `shared/sync-manager/.../domain/DomainPendingInspection.kt`
- Create: `shared/sync-manager/.../data/mappers/LocalRejectionReasonMapper.kt`
- Create: `shared/sync-manager/.../data/mappers/PendingInspectionMapper.kt`
- Create: `shared/sync-manager/.../commonTest/.../data/mappers/LocalRejectionReasonMapperTest.kt`
- Create: `shared/sync-manager/.../commonTest/.../data/mappers/PendingInspectionMapperTest.kt`

- [ ] **Step 1: Создать enum PendingInspectionStatus**

```kotlin
package ru.mirea.toir.sync.domain

enum class PendingInspectionStatus {
    COMPLETED,
    PARTIALLY_COMPLETED,
    CANCELLED,
}
```

- [ ] **Step 2: Создать enum InspectionRejectionReason**

```kotlin
package ru.mirea.toir.sync.domain

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

- [ ] **Step 3: Создать DomainPendingInspection**

```kotlin
package ru.mirea.toir.sync.domain

import kotlin.time.Instant

data class DomainPendingInspection(
    val inspectionId: String,
    val routeId: String,
    val assignmentId: String?,
    val completedAt: Instant?,
    val status: PendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: InspectionRejectionReason?,
)
```

- [ ] **Step 4: Failing test для LocalRejectionReasonMapper**

```kotlin
// .../commonTest/.../data/mappers/LocalRejectionReasonMapperTest.kt
package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.domain.InspectionRejectionReason
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalRejectionReasonMapperTest {

    @Test
    fun `every LocalRejectionReason maps to corresponding domain value`() {
        val expected = mapOf(
            LocalRejectionReason.INVALID_ASSIGNMENT_ID to InspectionRejectionReason.INVALID_ASSIGNMENT_ID,
            LocalRejectionReason.INVALID_ROUTE_ID to InspectionRejectionReason.INVALID_ROUTE_ID,
            LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN to InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
            LocalRejectionReason.ROUTE_ID_MISMATCH to InspectionRejectionReason.ROUTE_ID_MISMATCH,
            LocalRejectionReason.INSPECTION_NOT_FOUND to InspectionRejectionReason.INSPECTION_NOT_FOUND,
            LocalRejectionReason.ROUTE_POINT_NOT_FOUND to InspectionRejectionReason.ROUTE_POINT_NOT_FOUND,
            LocalRejectionReason.EQUIPMENT_MISMATCH to InspectionRejectionReason.EQUIPMENT_MISMATCH,
            LocalRejectionReason.UNKNOWN to InspectionRejectionReason.UNKNOWN,
        )
        expected.forEach { (local, domain) ->
            assertEquals(domain, local.toDomain())
        }
    }
}
```

- [ ] **Step 5: Запустить тест — должен упасть**

```bash
./gradlew :shared:sync-manager:allTests --tests "*LocalRejectionReasonMapperTest*"
```
Expected: FAIL — `toDomain` не определён.

- [ ] **Step 6: Реализовать маппер**

```kotlin
// .../data/mappers/LocalRejectionReasonMapper.kt
package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.domain.InspectionRejectionReason

internal fun LocalRejectionReason.toDomain(): InspectionRejectionReason = when (this) {
    LocalRejectionReason.INVALID_ASSIGNMENT_ID -> InspectionRejectionReason.INVALID_ASSIGNMENT_ID
    LocalRejectionReason.INVALID_ROUTE_ID -> InspectionRejectionReason.INVALID_ROUTE_ID
    LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    LocalRejectionReason.ROUTE_ID_MISMATCH -> InspectionRejectionReason.ROUTE_ID_MISMATCH
    LocalRejectionReason.INSPECTION_NOT_FOUND -> InspectionRejectionReason.INSPECTION_NOT_FOUND
    LocalRejectionReason.ROUTE_POINT_NOT_FOUND -> InspectionRejectionReason.ROUTE_POINT_NOT_FOUND
    LocalRejectionReason.EQUIPMENT_MISMATCH -> InspectionRejectionReason.EQUIPMENT_MISMATCH
    LocalRejectionReason.UNKNOWN -> InspectionRejectionReason.UNKNOWN
}
```

- [ ] **Step 7: Тест должен пройти**

```bash
./gradlew :shared:sync-manager:allTests --tests "*LocalRejectionReasonMapperTest*"
```
Expected: PASS.

- [ ] **Step 8: Failing test для PendingInspectionMapper**

```kotlin
// .../commonTest/.../data/mappers/PendingInspectionMapperTest.kt
package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.storage.inspection.models.LocalPendingInspection
import ru.mirea.toir.sync.domain.InspectionRejectionReason
import ru.mirea.toir.sync.domain.PendingInspectionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PendingInspectionMapperTest {

    @Test
    fun `maps completed inspection without rejection`() {
        val local = LocalPendingInspection(
            id = "ins-1",
            assignmentId = "asg-1",
            routeId = "route-1",
            status = LocalInspectionStatus.COMPLETED,
            completedAt = "2026-05-12T10:00:00Z",
            attemptCount = 0,
            rejectionReason = null,
        )
        val domain = local.toDomain()
        assertEquals("ins-1", domain.inspectionId)
        assertEquals("route-1", domain.routeId)
        assertEquals("asg-1", domain.assignmentId)
        assertEquals(Instant.parse("2026-05-12T10:00:00Z"), domain.completedAt)
        assertEquals(PendingInspectionStatus.COMPLETED, domain.status)
        assertEquals(0, domain.attemptCount)
        assertNull(domain.rejectionReason)
    }

    @Test
    fun `maps cancelled inspection with rejection reason`() {
        val local = LocalPendingInspection(
            id = "ins-2",
            assignmentId = null,
            routeId = "route-2",
            status = LocalInspectionStatus.CANCELLED,
            completedAt = null,
            attemptCount = 3,
            rejectionReason = LocalRejectionReason.ROUTE_ID_MISMATCH,
        )
        val domain = local.toDomain()
        assertEquals(PendingInspectionStatus.CANCELLED, domain.status)
        assertEquals(3, domain.attemptCount)
        assertEquals(InspectionRejectionReason.ROUTE_ID_MISMATCH, domain.rejectionReason)
        assertNull(domain.completedAt)
        assertNull(domain.assignmentId)
    }
}
```

- [ ] **Step 9: Запустить тест — должен упасть**

```bash
./gradlew :shared:sync-manager:allTests --tests "*PendingInspectionMapperTest*"
```
Expected: FAIL.

- [ ] **Step 10: Реализовать PendingInspectionMapper**

```kotlin
// .../data/mappers/PendingInspectionMapper.kt
package ru.mirea.toir.sync.data.mappers

import kotlin.time.Instant
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalPendingInspection
import ru.mirea.toir.sync.domain.DomainPendingInspection
import ru.mirea.toir.sync.domain.PendingInspectionStatus

internal fun LocalPendingInspection.toDomain(): DomainPendingInspection =
    DomainPendingInspection(
        inspectionId = id,
        routeId = routeId,
        assignmentId = assignmentId,
        completedAt = completedAt?.let(Instant::parse),
        status = status.toPendingStatus(),
        attemptCount = attemptCount.toInt(),
        rejectionReason = rejectionReason?.toDomain(),
    )

private fun LocalInspectionStatus.toPendingStatus(): PendingInspectionStatus = when (this) {
    LocalInspectionStatus.COMPLETED -> PendingInspectionStatus.COMPLETED
    LocalInspectionStatus.PARTIALLY_COMPLETED -> PendingInspectionStatus.PARTIALLY_COMPLETED
    LocalInspectionStatus.CANCELLED -> PendingInspectionStatus.CANCELLED
    LocalInspectionStatus.PLANNED, LocalInspectionStatus.IN_PROGRESS ->
        error("Non-final status $this should not appear in pending inspections query")
}
```

- [ ] **Step 11: Тест должен пройти**

```bash
./gradlew :shared:sync-manager:allTests --tests "*PendingInspectionMapperTest*"
```
Expected: PASS.

- [ ] **Step 12: Commit**

```bash
git add shared/sync-manager/
git commit -m "feat(sync): add DomainPendingInspection + mappers"
```

---

### Task 5: RemoteRejectedReason → LocalRejectionReason mapper (TDD)

**Files:**
- Create: `shared/sync-manager/.../data/mappers/RemoteRejectedReasonMapper.kt`
- Create: `shared/sync-manager/.../commonTest/.../data/mappers/RemoteRejectedReasonMapperTest.kt`

- [ ] **Step 1: Failing test**

```kotlin
package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteRejectedReasonMapperTest {

    @Test
    fun `every RemoteSyncRejectedReason maps to LocalRejectionReason`() {
        val expected = mapOf(
            RemoteSyncRejectedReason.INVALID_ASSIGNMENT_ID to LocalRejectionReason.INVALID_ASSIGNMENT_ID,
            RemoteSyncRejectedReason.INVALID_ROUTE_ID to LocalRejectionReason.INVALID_ROUTE_ID,
            RemoteSyncRejectedReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN to LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
            RemoteSyncRejectedReason.ROUTE_ID_MISMATCH to LocalRejectionReason.ROUTE_ID_MISMATCH,
            RemoteSyncRejectedReason.INSPECTION_NOT_FOUND to LocalRejectionReason.INSPECTION_NOT_FOUND,
            RemoteSyncRejectedReason.ROUTE_POINT_NOT_FOUND to LocalRejectionReason.ROUTE_POINT_NOT_FOUND,
            RemoteSyncRejectedReason.EQUIPMENT_MISMATCH to LocalRejectionReason.EQUIPMENT_MISMATCH,
            RemoteSyncRejectedReason.UNKNOWN to LocalRejectionReason.UNKNOWN,
        )
        expected.forEach { (remote, local) ->
            assertEquals(local, remote.toLocal())
        }
    }
}
```

- [ ] **Step 2: Run — should FAIL**

```bash
./gradlew :shared:sync-manager:allTests --tests "*RemoteRejectedReasonMapperTest*"
```

- [ ] **Step 3: Реализация**

```kotlin
// .../data/mappers/RemoteRejectedReasonMapper.kt
package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason

internal fun RemoteSyncRejectedReason.toLocal(): LocalRejectionReason = when (this) {
    RemoteSyncRejectedReason.INVALID_ASSIGNMENT_ID -> LocalRejectionReason.INVALID_ASSIGNMENT_ID
    RemoteSyncRejectedReason.INVALID_ROUTE_ID -> LocalRejectionReason.INVALID_ROUTE_ID
    RemoteSyncRejectedReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    RemoteSyncRejectedReason.ROUTE_ID_MISMATCH -> LocalRejectionReason.ROUTE_ID_MISMATCH
    RemoteSyncRejectedReason.INSPECTION_NOT_FOUND -> LocalRejectionReason.INSPECTION_NOT_FOUND
    RemoteSyncRejectedReason.ROUTE_POINT_NOT_FOUND -> LocalRejectionReason.ROUTE_POINT_NOT_FOUND
    RemoteSyncRejectedReason.EQUIPMENT_MISMATCH -> LocalRejectionReason.EQUIPMENT_MISMATCH
    RemoteSyncRejectedReason.UNKNOWN -> LocalRejectionReason.UNKNOWN
}
```

- [ ] **Step 4: Run — should PASS**

- [ ] **Step 5: Commit**

```bash
git add shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/mappers/RemoteRejectedReasonMapper.kt \
        shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/mappers/RemoteRejectedReasonMapperTest.kt
git commit -m "feat(sync): map RemoteSyncRejectedReason to LocalRejectionReason"
```

---

### Task 6: SyncRepository interface + SyncRepositoryImpl — rewrite handleRejected & scheduleBatchRetry & observe-методы

**Files:**
- Modify: `shared/sync-manager/.../domain/repository/SyncRepository.kt`
- Modify: `shared/sync-manager/.../data/repository/SyncRepositoryImpl.kt`

- [ ] **Step 1: SyncRepository — заменить observePendingCount**

```diff
- fun observePendingCount(): Flow<Long>
+ fun observeHasPending(): Flow<Boolean>
+ fun observePendingInspections(): Flow<List<DomainPendingInspection>>
```

- [ ] **Step 2: SyncRepositoryImpl — заменить observePendingCount**

Удалить текущую реализацию `observePendingCount` (combine 5 счётчиков). Добавить:

```kotlin
override fun observeHasPending(): Flow<Boolean> =
    inspectionStorage.observeHasPending()

override fun observePendingInspections(): Flow<List<DomainPendingInspection>> =
    inspectionStorage.observePendingInspections()
        .map { rows -> rows.map { it.toDomain() } }
```

Импорт: `ru.mirea.toir.sync.data.mappers.toDomain`.

- [ ] **Step 3: SyncRepositoryImpl — переписать handleRejected**

Найти `handleRejected(rejected: RemoteSyncRejected, ...)` (~ строка 352). Сейчас он вызывает `scheduleRetry(...)` со строковым reason. Изменить:

```kotlin
private fun handleRejected(
    rejected: RemoteSyncRejected,
    now: Instant,
    attemptsById: Map<Pair<RetryEntity, String>, Long>,
) {
    Napier.w(
        "Sync rejected: ${rejected.entityType} id=${rejected.entityId} reason=${rejected.reason}",
    )
    val entity = when (rejected.entityType) {
        RemoteSyncRejectedEntityType.INSPECTION -> RetryEntity.INSPECTION
        RemoteSyncRejectedEntityType.INSPECTION_EQUIPMENT_RESULT -> RetryEntity.EQUIPMENT_RESULT
        RemoteSyncRejectedEntityType.CHECKLIST_ITEM_RESULT -> RetryEntity.CHECKLIST_ITEM_RESULT
        RemoteSyncRejectedEntityType.UNKNOWN -> {
            Napier.e("Sync rejected with unknown entityType id=${rejected.entityId}; skipping")
            return
        }
    }
    val current = attemptsById[entity to rejected.entityId] ?: run {
        Napier.e(
            "Server rejected id=${rejected.entityId} (entity=$entity) which was not in pushed batch; skipping",
        )
        return
    }
    val localReason = rejected.reason.toLocal()
    val nextAttempt = current + 1
    val nextAt = nextAttemptIso(now, nextAttempt)
    when (entity) {
        RetryEntity.INSPECTION -> inspectionStorage.markInspectionRejected(
            id = rejected.entityId,
            attemptCount = nextAttempt,
            nextAttemptAt = nextAt,
            reason = localReason,
        )
        RetryEntity.EQUIPMENT_RESULT -> inspectionStorage.markEquipmentResultRejected(
            id = rejected.entityId,
            attemptCount = nextAttempt,
            nextAttemptAt = nextAt,
            reason = localReason,
        )
        RetryEntity.CHECKLIST_ITEM_RESULT -> inspectionStorage.markChecklistItemResultRejected(
            id = rejected.entityId,
            attemptCount = nextAttempt,
            nextAttemptAt = nextAt,
            reason = localReason,
        )
        RetryEntity.ACTION_LOG -> Napier.e("ActionLog reject not expected; skipping")
    }
}
```

- [ ] **Step 4: SyncRepositoryImpl — переписать scheduleBatchRetry**

Сейчас `scheduleBatchRetry` (вызывается при transport fail) пишет `reason` в каждую запись через `scheduleRetry(... reason = ...)`. Убрать передачу `reason`:

Заменить тело `scheduleBatchRetry`:
```kotlin
private fun scheduleBatchRetry(
    now: Instant,
    reason: String,  // больше не используется — удалить параметр и его прокидывание
    inspections: List<...>,
    equipmentResults: List<...>,
    checklistResults: List<...>,
    logs: List<...>,
) {
    // ...
}
```

Логически:
- удалить параметр `reason`
- удалить аргумент `reason` в вызовах `inspectionStorage.markInspectionRetryScheduled` и т.п. (это уже сделано в Task 3)
- удалить `reason` в `actionLogStorage.markRetryScheduled` если он тоже передавался

В call site (~ строка 102) — убрать `reason = throwable.toSyncFailureReason().name,`:
```diff
  scheduleBatchRetry(
      now = now,
-     reason = throwable.toSyncFailureReason().name,
      inspections = pendingInspections,
      ...
  )
```

- [ ] **Step 5: SyncRepositoryImpl — обновить старый private scheduleRetry**

Если есть приватный метод `scheduleRetry(entity, id, currentAttempt, now, reason: String)` — удалить параметр `reason` и не передавать его в storage-методы. Этот хелпер теперь только для transport-fail retry (без причины).

- [ ] **Step 6: Запустить тесты sync-manager**

```bash
./gradlew :shared:sync-manager:allTests
```
Expected: PASS (mapper-тесты + существующие).

- [ ] **Step 7: Commit**

```bash
git add shared/sync-manager/
git commit -m "refactor(sync): separate transport retry from business rejection storage"
```

---

### Task 7: SyncManager — заменить pendingCount

**Files:**
- Modify: `shared/sync-manager/.../domain/SyncManager.kt`

- [ ] **Step 1: Заменить публичное поле**

```diff
- val pendingCount: Flow<Long> = syncRepository.observePendingCount()
+ val hasPending: Flow<Boolean> = syncRepository.observeHasPending()
+ val pendingInspections: Flow<List<DomainPendingInspection>> = syncRepository.observePendingInspections()
```

Импорт: `ru.mirea.toir.sync.domain.DomainPendingInspection`.

- [ ] **Step 2: Сборка**

```bash
./gradlew :shared:sync-manager:compileKotlinMetadata
```
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/domain/SyncManager.kt
git commit -m "feat(sync): expose hasPending and pendingInspections from SyncManager"
```

---

### Task 8: routes-list/api — RoutesListPendingInspection + обновить RoutesListSyncIndicator

**Files:**
- Create: `shared/feature-routes-list/api/.../models/RoutesListPendingInspection.kt`
- Modify: `shared/feature-routes-list/api/.../models/RoutesListSyncIndicator.kt`
- Modify: `shared/feature-routes-list/api/.../store/RoutesListStore.kt`

- [ ] **Step 1: Создать модели**

```kotlin
// .../api/models/RoutesListPendingInspection.kt
package ru.mirea.toir.feature.routes.list.api.models

import kotlin.time.Instant

data class RoutesListPendingInspection(
    val inspectionId: String,
    val routeName: String?,
    val completedAt: Instant?,
    val status: RoutesListPendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: RoutesListRejectionReason?,
)

enum class RoutesListPendingInspectionStatus {
    COMPLETED,
    PARTIALLY_COMPLETED,
    CANCELLED,
}

enum class RoutesListRejectionReason {
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

- [ ] **Step 2: Обновить RoutesListSyncIndicator**

```diff
 data class RoutesListSyncIndicator(
-    val pendingCount: Int,
+    val hasPending: Boolean,
+    val pendingInspections: List<RoutesListPendingInspection>,
     val lastError: RoutesListSyncFailure?,
     val isRunning: Boolean,
     val lastSuccessAt: Instant?,
 )
```

- [ ] **Step 3: Обновить RoutesListStore.State**

Найти в `RoutesListStore.kt` поле `pendingCount: Int = 0` (~ строка 18) внутри `State`. Заменить:
```diff
data class State(
    ...
-   pendingCount = 0,
+   hasPending = false,
+   pendingInspections = emptyList(),
    ...
)
```

И в типе State аналогично.

- [ ] **Step 4: Сборка**

```bash
./gradlew :shared:feature-routes-list:api:compileKotlinMetadata
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-routes-list/api/
git commit -m "feat(routes-list/api): replace pendingCount with hasPending+inspections"
```

---

### Task 9: RoutesListStoreFactory / RoutesListReducer — обновить Message и ветки

**Files:**
- Modify: `shared/feature-routes-list/impl/.../domain/RoutesListStoreFactory.kt`
- Modify: `shared/feature-routes-list/impl/.../domain/RoutesListReducer.kt`
- Modify: `shared/feature-routes-list/impl/src/commonTest/.../domain/RoutesListReducerTest.kt`

- [ ] **Step 1: Failing test — обновить существующие кейсы в RoutesListReducerTest**

Найти тесты с `pendingCount = 3` / `pendingCount = 1` и переписать под новое API. Например:

```kotlin
@Test
fun `SyncPendingChanged updates hasPending and inspections list`() {
    val initial = State(/* defaults */)
    val pending = RoutesListPendingInspection(
        inspectionId = "ins-1",
        routeName = "КО-1",
        completedAt = Instant.parse("2026-05-12T10:00:00Z"),
        status = RoutesListPendingInspectionStatus.COMPLETED,
        attemptCount = 0,
        rejectionReason = null,
    )
    val msg = Message.SyncPendingChanged(hasPending = true, pendingInspections = listOf(pending))
    val result = reducer.reduce(initial, msg)
    assertTrue(result.syncIndicator.hasPending)
    assertEquals(listOf(pending), result.syncIndicator.pendingInspections)
}
```

(Точную сигнатуру Message и доступ к reducer.reduce уточнить по существующему коду теста — стиль не менять.)

Также добавить кейс для пустого списка с `hasPending = true` (фон): убедиться что флаг + пустой список валидны.

- [ ] **Step 2: Run test — should FAIL**

```bash
./gradlew :shared:feature-routes-list:impl:allTests --tests "*RoutesListReducerTest*"
```
Expected: FAIL (компиляция не пройдёт пока Message не обновлён).

- [ ] **Step 3: Обновить RoutesListStoreFactory.Message**

В `RoutesListStoreFactory.kt` найти `sealed class Message` (или `interface`) и заменить кейс `SyncPendingCountChanged(val count: Long)` (или похожий — точное имя см. в исходнике, было `Message.SyncPendingCountChanged`):

```diff
- data class SyncPendingCountChanged(val count: Long) : Message
+ data class SyncPendingChanged(
+     val hasPending: Boolean,
+     val pendingInspections: List<RoutesListPendingInspection>,
+ ) : Message
```

- [ ] **Step 4: Обновить ветку в Reducer**

```diff
- is Message.SyncPendingCountChanged -> copy(
-     syncIndicator = syncIndicator.copy(pendingCount = msg.count.toInt())
- )
+ is Message.SyncPendingChanged -> copy(
+     syncIndicator = syncIndicator.copy(
+         hasPending = msg.hasPending,
+         pendingInspections = msg.pendingInspections,
+     )
+ )
```

- [ ] **Step 5: Обновить Executor (StoreFactory.executor)**

Найти подписку на `syncManager.pendingCount` (~ внутри Executor.executeAction или init). Заменить:

```diff
- syncManager.pendingCount
-     .onEach { dispatch(Message.SyncPendingCountChanged(it)) }
-     .launchIn(scope)
+ combine(
+     syncManager.hasPending,
+     repository.observePendingInspections(),  // см. Task 10 — repository отдаёт уже Routes-Api-модель
+ ) { hasPending, list ->
+     Message.SyncPendingChanged(hasPending, list)
+ }.onEach(::dispatch).launchIn(scope)
```

(Точная имя метода repository уточняется в Task 10 — `RoutesListRepository.observePendingInspections(): Flow<List<RoutesListPendingInspection>>`.)

- [ ] **Step 6: Run tests — should PASS**

```bash
./gradlew :shared:feature-routes-list:impl:allTests --tests "*RoutesListReducerTest*"
```

- [ ] **Step 7: Commit**

```bash
git add shared/feature-routes-list/impl/
git commit -m "feat(routes-list): switch reducer to hasPending+inspections"
```

---

### Task 10: RoutesListRepositoryImpl — observe + routeName enrichment + Domain→Api mapper

**Files:**
- Modify: `shared/feature-routes-list/impl/.../data/repository/RoutesListRepositoryImpl.kt`

- [ ] **Step 1: Найти RoutesStorage / способ читать route name по routeId**

```bash
grep -rn "interface RoutesStorage\|fun selectRouteById\|routes_table\|class RoutesStorageImpl" /Users/a.dobrov/StudioProjects/toir-mobile/shared/core-database/
```

Использовать найденный метод; если такого метода нет — добавить в существующий RoutesStorage метод `suspend fun getRouteNameById(id: String): String?` (отдельный простой SQL `SELECT name FROM routes WHERE id = ?`). Если есть `selectRouteById` — использовать его и взять `.name`.

- [ ] **Step 2: Добавить mapper Domain→Api**

В тот же `RoutesListRepositoryImpl.kt` (внизу, рядом с существующим `SyncFailureReason.toApi()`):

```kotlin
private suspend fun DomainPendingInspection.toApi(): RoutesListPendingInspection {
    val routeName = routesStorage.getRouteNameById(routeId)  // имя метода — по итогам шага 1
    return RoutesListPendingInspection(
        inspectionId = inspectionId,
        routeName = routeName,
        completedAt = completedAt,
        status = status.toApi(),
        attemptCount = attemptCount,
        rejectionReason = rejectionReason?.toApi(),
    )
}

private fun PendingInspectionStatus.toApi(): RoutesListPendingInspectionStatus = when (this) {
    PendingInspectionStatus.COMPLETED -> RoutesListPendingInspectionStatus.COMPLETED
    PendingInspectionStatus.PARTIALLY_COMPLETED -> RoutesListPendingInspectionStatus.PARTIALLY_COMPLETED
    PendingInspectionStatus.CANCELLED -> RoutesListPendingInspectionStatus.CANCELLED
}

private fun InspectionRejectionReason.toApi(): RoutesListRejectionReason = when (this) {
    InspectionRejectionReason.INVALID_ASSIGNMENT_ID -> RoutesListRejectionReason.INVALID_ASSIGNMENT_ID
    InspectionRejectionReason.INVALID_ROUTE_ID -> RoutesListRejectionReason.INVALID_ROUTE_ID
    InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> RoutesListRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    InspectionRejectionReason.ROUTE_ID_MISMATCH -> RoutesListRejectionReason.ROUTE_ID_MISMATCH
    InspectionRejectionReason.INSPECTION_NOT_FOUND -> RoutesListRejectionReason.INSPECTION_NOT_FOUND
    InspectionRejectionReason.ROUTE_POINT_NOT_FOUND -> RoutesListRejectionReason.ROUTE_POINT_NOT_FOUND
    InspectionRejectionReason.EQUIPMENT_MISMATCH -> RoutesListRejectionReason.EQUIPMENT_MISMATCH
    InspectionRejectionReason.UNKNOWN -> RoutesListRejectionReason.UNKNOWN
}
```

- [ ] **Step 3: Добавить publicly доступную observePendingInspections**

В интерфейсе `RoutesListRepository` (или там, где есть `observeSyncIndicator`) — добавить:

```kotlin
fun observePendingInspections(): Flow<List<RoutesListPendingInspection>>
```

В `RoutesListRepositoryImpl`:

```kotlin
override fun observePendingInspections(): Flow<List<RoutesListPendingInspection>> =
    syncManager.pendingInspections
        .map { domainList -> domainList.map { it.toApi() } }
```

- [ ] **Step 4: Обновить observeSyncIndicator**

Сейчас в строках ~165-200 есть `combine(syncManager.pendingCount, ...)`. Заменить `syncManager.pendingCount` на `syncManager.hasPending`, и тип Long на Boolean.

`pendingInspections` НЕ кладём в `RoutesListSyncIndicator` через этот combine (избегаем мерж двух потоков с разной частотой обновления) — список приходит отдельным `observePendingInspections` в Executor (см. Task 9 Step 5). Но **поле в индикаторе всё равно есть** (Task 8) — оно заполняется в Reducer.

Если внутри `observeSyncIndicator` уже есть `pendingCount: Int = pending.toInt()` (строка ~184), заменить:
```diff
- pendingCount = pending.toInt()
+ hasPending = pending  // pending: Boolean
+ pendingInspections = emptyList()  // заполнится из отдельного observePendingInspections в Reducer
```

- [ ] **Step 5: Прокинуть RoutesStorage в зависимостях если нужно**

Если `RoutesListRepositoryImpl` ещё не получает `RoutesStorage` — добавить в конструктор и DI-модуль (`RoutesListImplModule.kt`).

- [ ] **Step 6: Сборка**

```bash
./gradlew :shared:feature-routes-list:impl:compileKotlinMetadata
```
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add shared/feature-routes-list/impl/
git commit -m "feat(routes-list): wire pendingInspections from SyncManager to UI through repository"
```

---

### Task 11: Presentation — UiPendingInspection + mapper

**Files:**
- Create: `shared/feature-routes-list/presentation/.../models/UiPendingInspection.kt`
- Modify: `shared/feature-routes-list/presentation/.../models/UiRoutesListState.kt`
- Modify: `shared/feature-routes-list/presentation/.../mappers/UiRoutesListStateMapper.kt`

- [ ] **Step 1: Создать UiPendingInspection**

```kotlin
// .../presentation/models/UiPendingInspection.kt
package ru.mirea.toir.feature.routes.list.presentation.models

import kotlin.time.Instant

data class UiPendingInspection(
    val inspectionId: String,
    val routeName: String?,
    val completedAt: Instant?,
    val status: UiPendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: UiRejectionReason?,
)

enum class UiPendingInspectionStatus { COMPLETED, PARTIALLY_COMPLETED, CANCELLED }

enum class UiRejectionReason {
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

- [ ] **Step 2: UiRoutesListState — заменить pendingCount на hasPending + список**

В файле `UiRoutesListState.kt` найти секцию sync indicator (~ строка 18 `pendingCount: Int = 0`):

```diff
data class UiSyncIndicator(
    val isRunning: Boolean = false,
-   val pendingCount: Int = 0,
+   val hasPending: Boolean = false,
+   val pendingInspections: List<UiPendingInspection> = emptyList(),
    val lastError: UiSyncFailure? = null,
    ...
)
```

- [ ] **Step 3: UiRoutesListStateMapper — добавить маппинг**

В `UiRoutesListStateMapper.kt` (~ строка 39-52, где сейчас `pendingCount = pendingCount`):

```diff
- pendingCount = pendingCount
+ hasPending = hasPending,
+ pendingInspections = pendingInspections.map { it.toUi() },
```

И добавить extension `toUi()`:

```kotlin
private fun RoutesListPendingInspection.toUi(): UiPendingInspection =
    UiPendingInspection(
        inspectionId = inspectionId,
        routeName = routeName,
        completedAt = completedAt,
        status = status.toUi(),
        attemptCount = attemptCount,
        rejectionReason = rejectionReason?.toUi(),
    )

private fun RoutesListPendingInspectionStatus.toUi(): UiPendingInspectionStatus = when (this) {
    RoutesListPendingInspectionStatus.COMPLETED -> UiPendingInspectionStatus.COMPLETED
    RoutesListPendingInspectionStatus.PARTIALLY_COMPLETED -> UiPendingInspectionStatus.PARTIALLY_COMPLETED
    RoutesListPendingInspectionStatus.CANCELLED -> UiPendingInspectionStatus.CANCELLED
}

private fun RoutesListRejectionReason.toUi(): UiRejectionReason = when (this) {
    RoutesListRejectionReason.INVALID_ASSIGNMENT_ID -> UiRejectionReason.INVALID_ASSIGNMENT_ID
    RoutesListRejectionReason.INVALID_ROUTE_ID -> UiRejectionReason.INVALID_ROUTE_ID
    RoutesListRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> UiRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    RoutesListRejectionReason.ROUTE_ID_MISMATCH -> UiRejectionReason.ROUTE_ID_MISMATCH
    RoutesListRejectionReason.INSPECTION_NOT_FOUND -> UiRejectionReason.INSPECTION_NOT_FOUND
    RoutesListRejectionReason.ROUTE_POINT_NOT_FOUND -> UiRejectionReason.ROUTE_POINT_NOT_FOUND
    RoutesListRejectionReason.EQUIPMENT_MISMATCH -> UiRejectionReason.EQUIPMENT_MISMATCH
    RoutesListRejectionReason.UNKNOWN -> UiRejectionReason.UNKNOWN
}
```

- [ ] **Step 4: Сборка**

```bash
./gradlew :shared:feature-routes-list:presentation:compileKotlinMetadata
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-routes-list/presentation/
git commit -m "feat(routes-list/presentation): map pending inspections to UI model"
```

---

### Task 12: MR.strings — добавить новые + удалить старые

**Files:**
- Modify: файл строковых ресурсов (найти через `grep -rn "sync_status_pending_label" /Users/a.dobrov/StudioProjects/toir-mobile/shared/` — обычно `core-common-ui/src/commonMain/resources/MR/base/strings.xml` или подобное)

- [ ] **Step 1: Найти файл**

```bash
grep -rln "sync_status_pending_label" /Users/a.dobrov/StudioProjects/toir-mobile/shared/
```

- [ ] **Step 2: Удалить устаревшие ключи**

```xml
<!-- удалить -->
<string name="sync_status_pending_label">…</string>
<string name="sync_status_pending_zero">…</string>
```

- [ ] **Step 3: Добавить новые**

```xml
<string name="sync_status_pending_list_title">Ожидают отправки</string>
<string name="sync_status_pending_list_empty">Все обходы синхронизированы</string>
<string name="sync_status_pending_in_background">Данные отправляются в фоне</string>
<string name="sync_status_pending_attempts">попыток: %d</string>
<string name="sync_status_pending_rejected_prefix">Обход отвергнут</string>

<string name="sync_rejection_invalid_assignment_id">недопустимое задание</string>
<string name="sync_rejection_invalid_route_id">недопустимый маршрут</string>
<string name="sync_rejection_route_assignment_not_found">задание не найдено или нет доступа</string>
<string name="sync_rejection_route_id_mismatch">несоответствие маршрута</string>
<string name="sync_rejection_inspection_not_found">обход не найден</string>
<string name="sync_rejection_route_point_not_found">точка маршрута не найдена</string>
<string name="sync_rejection_equipment_mismatch">несоответствие оборудования</string>
<string name="sync_rejection_unknown">неизвестная причина</string>
```

- [ ] **Step 4: Сборка для генерации MR**

```bash
./gradlew :shared:core-common-ui:compileKotlinMetadata
```
(или подходящий модуль ресурсов)
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add <ресурсный файл>
git commit -m "feat(strings): pending inspections list + rejection reasons"
```

---

### Task 13: SyncIndicatorIcon — убрать бэйдж

**Files:**
- Modify: `shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/components/SyncIndicatorIcon.kt`

- [ ] **Step 1: Удалить Badge с числом**

В файле `SyncIndicatorIcon.kt` строки ~43-49 (`BadgedBox`/`Badge { Text(indicator.pendingCount.toString()) }`). Удалить весь блок `BadgedBox`, оставив только `Icon`. Замена `pendingCount > 0` на `hasPending`:

```kotlin
@Composable
fun SyncIndicatorIcon(
    indicator: UiSyncIndicator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (iconRes, tint) = pickIcon(indicator)
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(MR.strings.sync_indicator_content_description),
            tint = tint,
        )
    }
}

private fun pickIcon(indicator: UiSyncIndicator): Pair<ImageResource, Color> = when {
    indicator.isRunning -> MR.images.ic_sync to colors.accent
    indicator.lastError != null -> MR.images.ic_sync_off to colors.error
    indicator.hasPending -> MR.images.ic_sync_alt to colors.warning
    else -> MR.images.ic_sync to colors.onSurface
}
```

(Точные имена colors/Image зависят от существующего кода — оставить как сейчас, заменить только условие `pendingCount > 0` на `hasPending`.)

- [ ] **Step 2: Сборка**

```bash
./gradlew :shared:feature-routes-list:ui:compileKotlinMetadata
```
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add shared/feature-routes-list/ui/src/commonMain/kotlin/ru/mirea/toir/feature/routes/list/ui/components/SyncIndicatorIcon.kt
git commit -m "feat(ui): remove pending count badge from sync indicator"
```

---

### Task 14: SyncStatusBottomSheet — заменить PendingCountCard на PendingInspectionsSection

**Files:**
- Modify: `shared/feature-routes-list/ui/.../components/SyncStatusBottomSheet.kt`
- Create: `shared/feature-routes-list/ui/.../components/PendingInspectionsSection.kt`

- [ ] **Step 1: Удалить PendingCountCard из BottomSheet**

В `SyncStatusBottomSheet.kt` найти `PendingCountCard(indicator = indicator)` (~ строка 61) и сам composable `PendingCountCard` (~ строка 139). Удалить обе вещи.

- [ ] **Step 2: Создать PendingInspectionsSection**

```kotlin
// .../components/PendingInspectionsSection.kt
package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.MR
import ru.mirea.toir.feature.routes.list.presentation.models.UiPendingInspection
import ru.mirea.toir.feature.routes.list.presentation.models.UiRejectionReason
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator

@Composable
internal fun PendingInspectionsSection(
    indicator: UiSyncIndicator,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(MR.strings.sync_status_pending_list_title),
            style = MaterialTheme.typography.titleMedium,
        )
        when {
            indicator.pendingInspections.isEmpty() && !indicator.hasPending ->
                Text(stringResource(MR.strings.sync_status_pending_list_empty))
            indicator.pendingInspections.isEmpty() && indicator.hasPending ->
                Text(stringResource(MR.strings.sync_status_pending_in_background))
            else -> indicator.pendingInspections.forEach { item ->
                PendingInspectionCard(item)
            }
        }
    }
}

@Composable
private fun PendingInspectionCard(item: UiPendingInspection) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.routeName ?: item.inspectionId,
                style = MaterialTheme.typography.bodyLarge,
            )
            item.completedAt?.let {
                Text(
                    text = it.toString(),  // TODO formatting helper уже есть в проекте — использовать
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            when {
                item.rejectionReason != null -> Text(
                    text = stringResource(MR.strings.sync_status_pending_rejected_prefix) +
                        ": " +
                        stringResource(item.rejectionReason.toMessageRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                item.attemptCount > 0 -> Text(
                    text = stringResource(MR.strings.sync_status_pending_attempts, item.attemptCount),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun UiRejectionReason.toMessageRes() = when (this) {
    UiRejectionReason.INVALID_ASSIGNMENT_ID -> MR.strings.sync_rejection_invalid_assignment_id
    UiRejectionReason.INVALID_ROUTE_ID -> MR.strings.sync_rejection_invalid_route_id
    UiRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> MR.strings.sync_rejection_route_assignment_not_found
    UiRejectionReason.ROUTE_ID_MISMATCH -> MR.strings.sync_rejection_route_id_mismatch
    UiRejectionReason.INSPECTION_NOT_FOUND -> MR.strings.sync_rejection_inspection_not_found
    UiRejectionReason.ROUTE_POINT_NOT_FOUND -> MR.strings.sync_rejection_route_point_not_found
    UiRejectionReason.EQUIPMENT_MISMATCH -> MR.strings.sync_rejection_equipment_mismatch
    UiRejectionReason.UNKNOWN -> MR.strings.sync_rejection_unknown
}
```

(Format completedAt — найти уже существующий хелпер `formatDateTime`/подобное в проекте; не вводим новый.)

- [ ] **Step 3: Вставить секцию в SyncStatusBottomSheet**

В `SyncStatusBottomSheet.kt` на месте удалённого `PendingCountCard(indicator = indicator)` (~ строка 61):

```kotlin
PendingInspectionsSection(indicator = indicator)
```

- [ ] **Step 4: Сборка**

```bash
./gradlew :shared:feature-routes-list:ui:compileKotlinMetadata
./gradlew :android:app:assembleDebug
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-routes-list/ui/
git commit -m "feat(ui): replace pending count card with inspections list section"
```

---

### Task 15: Финальная проверка — сборка всего + детект + smoke

- [ ] **Step 1: Полная сборка**

```bash
cd /Users/a.dobrov/StudioProjects/toir-mobile
./gradlew :android:app:assembleDebug
./gradlew :shared:sync-manager:assemble
./gradlew :shared:feature-routes-list:impl:assemble
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Все тесты**

```bash
./gradlew allTests
```
Expected: PASS.

- [ ] **Step 3: Detekt**

```bash
./gradlew detekt
```
Expected: clean (или те же warnings, что и до PR).

- [ ] **Step 4: Smoke на устройстве**

1. Очистить data приложения (`adb shell pm clear ru.mirea.toir`).
2. Запустить, залогиниться.
3. Открыть индикатор синхронизации (TopAppBar) — проверить что бэйдж с цифрой пропал.
4. Открыть bottom sheet — состояние «Всё синхронизировано».
5. Включить airplane mode, завершить один обход → bottom sheet показывает обход в секции «Ожидают отправки» без причины.
6. Выключить airplane mode → секция исчезает в течение ~5 секунд (sync run + connectivity trigger).
7. (Опционально) Имитировать bizz-reject: подменить через MITM или dev-флаг — UI должен показать «Обход отвергнут: <причина>».

---

## Self-Review (done before user)

- ✅ Все секции spec покрыты задачами: schema (T1-T2), storage (T3), domain+mappers (T4-T5), repository rewrite (T6), SyncManager (T7), routes-list/api (T8), reducer/store (T9), repository wiring (T10), presentation (T11), strings (T12), UI (T13-T14), verification (T15).
- ✅ Нет placeholder'ов «TBD» / «implement later».
- ✅ Имена методов/типов/полей согласованы по всему плану.
- ✅ Известный issue (zombie retry на bizz-rejection) явно вынесен из scope в spec и НЕ маскируется в плане.
