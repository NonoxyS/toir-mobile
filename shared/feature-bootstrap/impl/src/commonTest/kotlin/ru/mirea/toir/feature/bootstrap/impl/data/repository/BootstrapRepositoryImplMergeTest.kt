@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.feature.bootstrap.impl.data.repository

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.core.database.storage.user.UserStorageImpl
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.FakeBootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.bootstrapResponseWithSingleRestoredTree
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.remoteChecklistItemResult
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.remoteEquipmentResult
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.remoteInspection
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.remotePhoto
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.seedConfigSkeleton
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.seedLocalChecklistItemResult
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.seedLocalEquipmentResult
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestData.seedLocalInspection
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.TestDatabase
import ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures.testDispatchers
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult

/**
 * Verifies Waypoint 11 §1.3 merge rule end-to-end through `BootstrapRepositoryImpl`:
 *  - INSERT when missing
 *  - UPDATE when local is `synced`
 *  - SKIP (preserve local) when local is `pending`/`retry_scheduled`/`rejected`
 *
 * The merge logic itself lives in SQL (`upsertFromServer` in Inspection.sq /
 * InspectionEquipmentResult.sq / ChecklistItemResult.sq and `insertRestoredPhoto`
 * in Photo.sq). These tests prove the repository wires up those SQL calls correctly
 * AND wraps them in a transaction, so the contract holds at the layer that the
 * rest of the app sees.
 */
internal class BootstrapRepositoryImplMergeTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val dispatchers = testDispatchers()

    private val inspectionStorage = InspectionStorageImpl(db, dispatchers)
    private val photoStorage = PhotoStorageImpl(db, dispatchers)
    private val transactionRunner = TransactionRunnerImpl(db)

    private fun repository(response: RemoteBootstrapResponse) =
        BootstrapRepositoryImpl(
            apiClient = FakeBootstrapApiClient(response),
            userStorage = UserStorageImpl(db),
            equipmentStorage = EquipmentStorageImpl(db, dispatchers),
            locationStorage = LocationStorageImpl(db),
            routeStorage = RouteStorageImpl(db, dispatchers),
            checklistStorage = ChecklistStorageImpl(db, dispatchers),
            syncMetaStorage = SyncMetaStorageImpl(db, dispatchers),
            inspectionStorage = inspectionStorage,
            photoStorage = photoStorage,
            transactionRunner = transactionRunner,
            coroutineDispatchers = dispatchers,
        )

    @AfterTest
    fun tearDown() = driver.close()

    /**
     * Scenario 1 — empty local DB. After wipe / reinstall the user logs in and
     * server pushes the in-flight inspection back. Everything must land as
     * `synced` (it's the server's truth, just landed locally for the first time).
     */
    @Test
    fun `empty local db — restored tree is inserted with sync_status synced`() = runTest {
        db.seedConfigSkeleton()
        val repo = repository(bootstrapResponseWithSingleRestoredTree())

        val result = repo.loadAndSaveBootstrap()

        assertEquals(BootstrapResult.Success, result)

        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection)
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)
        assertEquals(LocalInspectionStatus.IN_PROGRESS, inspection.status)

        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier)
        assertEquals(LocalSyncStatus.SYNCED, ier.syncStatus)
        assertEquals(LocalEquipmentResultStatus.IN_PROGRESS, ier.status)

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir)
        assertEquals(LocalSyncStatus.SYNCED, cir.syncStatus)
        assertEquals(1L, cir.valueBoolean)

        val photos = photoStorage.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
        assertEquals(1, photos.size)
        val photo = photos.single()
        assertEquals(LocalSyncStatus.SYNCED, photo.syncStatus)
        assertEquals(null, photo.fileUri) // file is downloaded later in Phase 5
        assertEquals("photo.jpg", photo.fileName)
        assertEquals(1024L, photo.sizeBytes)
    }

    /**
     * Scenario 2 — all local records already `synced`. Re-bootstrap (cold start)
     * arrives with identical server data. State must remain `synced` and identical.
     * This is the "happy steady-state" hit on every restart.
     */
    @Test
    fun `all local synced — re-bootstrap with identical data is a no-op`() = runTest {
        db.seedConfigSkeleton()
        db.seedLocalInspection(syncStatus = LocalSyncStatus.SYNCED)
        db.seedLocalEquipmentResult(syncStatus = LocalSyncStatus.SYNCED)
        db.seedLocalChecklistItemResult(syncStatus = LocalSyncStatus.SYNCED)

        val repo = repository(bootstrapResponseWithSingleRestoredTree())

        val result = repo.loadAndSaveBootstrap()

        assertEquals(BootstrapResult.Success, result)

        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection)
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)
        assertEquals(LocalInspectionStatus.IN_PROGRESS, inspection.status)

        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier)
        assertEquals(LocalSyncStatus.SYNCED, ier.syncStatus)

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir)
        assertEquals(LocalSyncStatus.SYNCED, cir.syncStatus)
    }

    /**
     * Scenario 3 — local `pending`, server returns older / stale copy. SQL guard
     * `WHERE sync_status = 'synced'` must skip the UPDATE; local pending value
     * must survive untouched. This is the load-bearing case: blowing it away =
     * data loss for the user. Asserts every field on the row to catch any partial
     * write.
     */
    @Test
    fun `local pending — server copy does not overwrite pending row`() = runTest {
        db.seedConfigSkeleton()
        // Local pending CIR with the user's actual answer: comment "user comment".
        db.seedLocalInspection(
            status = LocalInspectionStatus.IN_PROGRESS,
            startedAt = TestData.NOW,
            completedAt = null,
            syncStatus = LocalSyncStatus.PENDING,
        )
        db.seedLocalEquipmentResult(
            status = LocalEquipmentResultStatus.IN_PROGRESS,
            startedAt = TestData.NOW,
            completedAt = null,
            syncStatus = LocalSyncStatus.PENDING,
        )
        db.seedLocalChecklistItemResult(
            valueBoolean = 1L,
            comment = "user comment",
            syncStatus = LocalSyncStatus.PENDING,
        )

        // Snapshot the seeded pending rows BEFORE the merge to assert byte-for-byte
        // identity after — catches any partial UPDATE that leaks fields not covered
        // by the explicit per-field assertions below (e.g. assignmentId / routeId /
        // createdAt / updatedAt / startedAt / sync_attempt_count). The SQL guard
        // `WHERE sync_status = 'synced'` is binary in theory, but a future bug in
        // an UPDATE SET clause can't slip past a full-row equality check.
        val inspectionBefore = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        val ierBefore = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        val cirBefore = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(inspectionBefore)
        assertNotNull(ierBefore)
        assertNotNull(cirBefore)

        // Server claims the inspection is COMPLETED (stale view, e.g. previous
        // sync attempt) with a different completedAt timestamp. Should NOT win.
        val response = RemoteBootstrapResponse(
            user = null,
            device = null,
            assignments = emptyList(),
            routes = emptyList(),
            routePoints = emptyList(),
            equipment = emptyList(),
            locations = emptyList(),
            checklists = emptyList(),
            checklistItems = emptyList(),
            inspections = listOf(
                remoteInspection(
                    status = "completed",
                    startedAt = TestData.EARLIER,
                    completedAt = TestData.NOW,
                    updatedAt = TestData.NOW,
                ),
            ),
            inspectionEquipmentResults = listOf(
                remoteEquipmentResult(
                    status = "completed",
                    startedAt = TestData.EARLIER,
                    completedAt = TestData.NOW,
                    updatedAt = TestData.NOW,
                ),
            ),
            checklistItemResults = listOf(
                remoteChecklistItemResult(
                    valueBoolean = false,
                    comment = "server-side different comment",
                    updatedAt = TestData.NOW,
                ),
            ),
            photos = emptyList(),
            serverTime = TestData.NOW,
        )

        val result = repository(response).loadAndSaveBootstrap()

        assertEquals(BootstrapResult.Success, result)

        // Inspection: pending row preserved — status, completedAt, sync_status all unchanged.
        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection)
        assertEquals(LocalSyncStatus.PENDING, inspection.syncStatus)
        assertEquals(LocalInspectionStatus.IN_PROGRESS, inspection.status)
        assertEquals(null, inspection.completedAt)

        // IER: pending row preserved.
        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier)
        assertEquals(LocalSyncStatus.PENDING, ier.syncStatus)
        assertEquals(LocalEquipmentResultStatus.IN_PROGRESS, ier.status)
        assertEquals(null, ier.completedAt)

        // CIR: user's comment + boolean preserved, NOT clobbered with server's.
        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir)
        assertEquals(LocalSyncStatus.PENDING, cir.syncStatus)
        assertEquals(1L, cir.valueBoolean)
        assertEquals("user comment", cir.comment)

        // Full-row structural equality — every field on the row must be byte-for-byte
        // unchanged. `LocalInspection`, `LocalEquipmentResult`, `LocalChecklistItemResult`
        // are data classes, so this covers fields the explicit asserts above don't
        // mention: assignmentId, routeId, startedAt, createdAt, updatedAt,
        // syncAttemptCount, syncNextAttemptAt, syncRejectionReason — and analogous
        // for IER/CIR.
        assertEquals(inspectionBefore, inspection)
        assertEquals(ierBefore, ier)
        assertEquals(cirBefore, cir)
    }

    /**
     * Scenario 4 — partial pending tree: inspection + IER are `synced`, but one
     * CIR underneath is `pending` (user just answered the question, sync hasn't
     * run yet). Server bootstrap arrives with its older view of the same CIR.
     * Synced rows refresh (no-op since identical), pending CIR must be preserved.
     * Tests row-level granularity of the merge rule (§1.3 "Уровень — целая строка").
     */
    @Test
    fun `partial pending tree — synced rows refresh and pending CIR preserved`() = runTest {
        db.seedConfigSkeleton()
        db.seedLocalInspection(syncStatus = LocalSyncStatus.SYNCED)
        db.seedLocalEquipmentResult(syncStatus = LocalSyncStatus.SYNCED)
        // Pending CIR with the user's just-typed comment.
        db.seedLocalChecklistItemResult(
            valueBoolean = 0L,
            comment = "freshly entered, not yet synced",
            syncStatus = LocalSyncStatus.PENDING,
        )

        // Server view of the same CIR has different boolean + no comment.
        val response = RemoteBootstrapResponse(
            user = null,
            device = null,
            assignments = emptyList(),
            routes = emptyList(),
            routePoints = emptyList(),
            equipment = emptyList(),
            locations = emptyList(),
            checklists = emptyList(),
            checklistItems = emptyList(),
            inspections = listOf(remoteInspection()),
            inspectionEquipmentResults = listOf(remoteEquipmentResult()),
            checklistItemResults = listOf(
                remoteChecklistItemResult(valueBoolean = true, comment = null),
            ),
            photos = emptyList(),
            serverTime = TestData.NOW,
        )

        val result = repository(response).loadAndSaveBootstrap()

        assertEquals(BootstrapResult.Success, result)

        // Synced parent rows still synced (UPDATE happened, no-op semantically).
        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection)
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)

        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier)
        assertEquals(LocalSyncStatus.SYNCED, ier.syncStatus)

        // Pending CIR preserved — boolean and comment as the user typed them.
        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir)
        assertEquals(LocalSyncStatus.PENDING, cir.syncStatus)
        assertEquals(0L, cir.valueBoolean)
        assertEquals("freshly entered, not yet synced", cir.comment)
    }

    /**
     * Scenario 5 — run loadAndSaveBootstrap twice with the same response. Verifies:
     * no duplicate rows (PK conflict semantics), no clobbered state (idempotency).
     * Bootstrap fires on every cold start (`App.kt:31`), so this isn't theoretical.
     */
    @Test
    fun `re-run idempotency — second bootstrap is no-op without duplicates`() = runTest {
        db.seedConfigSkeleton()
        val response = bootstrapResponseWithSingleRestoredTree()
        val repo = repository(response)

        val first = repo.loadAndSaveBootstrap()
        assertEquals(BootstrapResult.Success, first)

        val inspectionAfterFirst = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspectionAfterFirst)

        val second = repo.loadAndSaveBootstrap()
        assertEquals(BootstrapResult.Success, second)

        // Same row, same status, no duplicates.
        val inspectionAfterSecond = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspectionAfterSecond)
        assertEquals(LocalSyncStatus.SYNCED, inspectionAfterSecond.syncStatus)
        assertEquals(inspectionAfterFirst.id, inspectionAfterSecond.id)

        val ierList = inspectionStorage.selectEquipmentResultsByInspectionId(TestData.INSPECTION_ID)
        assertEquals(1, ierList.size)
        assertEquals(LocalSyncStatus.SYNCED, ierList.single().syncStatus)

        val cirList = inspectionStorage.selectChecklistItemResultsByEquipmentResult(
            TestData.EQUIPMENT_RESULT_ID,
        )
        assertEquals(1, cirList.size)
        assertEquals(LocalSyncStatus.SYNCED, cirList.single().syncStatus)

        val photos = photoStorage.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
        assertEquals(1, photos.size)
        assertEquals(LocalSyncStatus.SYNCED, photos.single().syncStatus)
        assertEquals(null, photos.single().fileUri)
    }

    /**
     * Bonus check: photo merge follows the same rule. Local `pending` photo
     * (uploaded-locally, not yet pushed) must NOT be touched by
     * `insertRestoredPhoto` even when the server returns a same-id photo. This
     * is the Phase 3 followup behaviour wired through bootstrap; included here so
     * a regression that drops the photo branch surfaces immediately.
     */
    @Test
    fun `pending photo — server metadata does not overwrite pending row`() = runTest {
        db.seedConfigSkeleton()
        db.seedLocalInspection(syncStatus = LocalSyncStatus.SYNCED)
        db.seedLocalEquipmentResult(syncStatus = LocalSyncStatus.SYNCED)
        db.seedLocalChecklistItemResult(syncStatus = LocalSyncStatus.SYNCED)
        // Local pending photo with real file_uri (taken on device, not yet uploaded).
        db.photoQueries.insertPhoto(
            id = TestData.PHOTO_ID,
            checklist_item_result_id = TestData.CHECKLIST_ITEM_RESULT_ID,
            file_uri = "file:///tmp/local-photo.jpg",
            taken_at = TestData.NOW,
            sync_status = LocalSyncStatus.PENDING,
            storage_key = null,
        )

        val response = RemoteBootstrapResponse(
            user = null,
            device = null,
            assignments = emptyList(),
            routes = emptyList(),
            routePoints = emptyList(),
            equipment = emptyList(),
            locations = emptyList(),
            checklists = emptyList(),
            checklistItems = emptyList(),
            inspections = listOf(remoteInspection()),
            inspectionEquipmentResults = listOf(remoteEquipmentResult()),
            checklistItemResults = listOf(remoteChecklistItemResult()),
            photos = listOf(remotePhoto()),
            serverTime = TestData.NOW,
        )

        val result = repository(response).loadAndSaveBootstrap()

        assertEquals(BootstrapResult.Success, result)

        val photos = photoStorage.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
        assertEquals(1, photos.size)
        val photo = photos.single()
        // Pending row preserved: file_uri NOT nulled, syncStatus unchanged.
        assertEquals(LocalSyncStatus.PENDING, photo.syncStatus)
        assertEquals("file:///tmp/local-photo.jpg", photo.fileUri)
    }
}
