@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.sync.data.applier.ConfigChangesApplier
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.fixtures.TestData
import ru.mirea.toir.sync.fixtures.TestData.seedFullPendingScenario
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestSyncApi.Companion.respondJson
import ru.mirea.toir.sync.fixtures.TestTokenStorage
import ru.mirea.toir.sync.fixtures.testDispatchers

class SyncRepositoryPushTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val syncApi = TestSyncApi()
    private val dispatchers = testDispatchers()

    private val repo: SyncRepository = SyncRepositoryImpl(
        syncApiClient = syncApi.build(),
        inspectionStorage = InspectionStorageImpl(db, dispatchers),
        photoStorage = PhotoStorageImpl(db, dispatchers),
        actionLogStorage = ActionLogStorageImpl(db, dispatchers),
        syncMetaStorage = SyncMetaStorageImpl(db, dispatchers),
        tokenStorage = TestTokenStorage(),
        configChangesApplier = ConfigChangesApplier(
            routeStorage = RouteStorageImpl(db, dispatchers),
            equipmentStorage = EquipmentStorageImpl(db, dispatchers),
            locationStorage = LocationStorageImpl(db),
            checklistStorage = ChecklistStorageImpl(db, dispatchers),
            transactionRunner = TransactionRunnerImpl(db),
        ),
        transactionRunner = TransactionRunnerImpl(db),
        coroutineDispatchers = dispatchers,
    )

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `pushPendingData with one pending inspection - accepted - marked SYNCED`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush {
            respondJson(
                """
                {
                    "clientBatchId":"server-batch-1",
                    "result":"accepted",
                    "accepted":{
                        "inspections":["${TestData.INSPECTION_ID}"],
                        "inspectionEquipmentResults":["${TestData.EQUIPMENT_RESULT_ID}"],
                        "checklistItemResults":["${TestData.CHECKLIST_ITEM_RESULT_ID}"],
                        "actionLogs":[]
                    },
                    "rejected":[],
                    "serverTime":"2026-05-13T12:00:00Z"
                }
            """.trimIndent()
            )
        }

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess, "Push failed: $result")
        val syncResult = result.getOrThrow()
        assertEquals(3, syncResult.acceptedCount)
        assertEquals(0, syncResult.rejectedCount)

        val inspection = db.inspectionQueries.selectById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.SYNCED, inspection.sync_status)
    }

    @Test
    fun `pushPendingData with rejected inspection - attempt count incremented`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush {
            respondJson(
                """
                {
                    "clientBatchId":"server-batch-2",
                    "result":"accepted",
                    "accepted":{"inspections":[],"inspectionEquipmentResults":[],"checklistItemResults":[],"actionLogs":[]},
                    "rejected":[
                        {
                            "entityType":"inspection",
                            "entityId":"${TestData.INSPECTION_ID}",
                            "reason":"INSPECTION_NOT_FOUND"
                        }
                    ],
                    "serverTime":"2026-05-13T12:00:00Z"
                }
            """.trimIndent()
            )
        }

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess, "Push failed: $result")
        val syncResult = result.getOrThrow()
        assertEquals(0, syncResult.acceptedCount)
        assertEquals(1, syncResult.rejectedCount)

        val inspection = db.inspectionQueries.selectById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, inspection.sync_status)
        assertEquals(1L, inspection.sync_attempt_count)
        assertTrue(inspection.sync_next_attempt_at != null, "next_attempt_at should be set after reject")
    }

    @Test
    fun `pushPendingData with no pending - succeeds without HTTP call`() = runTest {
        // No seeding — empty database.

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess)
        val syncResult = result.getOrThrow()
        assertEquals(0, syncResult.acceptedCount)
        assertEquals(0, syncResult.rejectedCount)
        assertEquals(0, syncApi.capturedRequests.count { it.url.encodedPath.contains("/api/v1/mobile/sync/push") })
    }

    @Test
    fun `pushPendingData HTTP 500 - Result_failure - rows stay PENDING with incremented attempt`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush {
            with(TestSyncApi) { respondJson("server down", io.ktor.http.HttpStatusCode.InternalServerError) }
        }

        val result = repo.pushPendingData()

        assertTrue(result.isFailure, "Expected failure but was $result")

        val inspection = db.inspectionQueries.selectById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, inspection.sync_status)
        assertEquals(1L, inspection.sync_attempt_count, "scheduleBatchRetry should increment attempt count")
        assertTrue(inspection.sync_next_attempt_at != null, "next_attempt_at should be set after retry schedule")
    }
}
