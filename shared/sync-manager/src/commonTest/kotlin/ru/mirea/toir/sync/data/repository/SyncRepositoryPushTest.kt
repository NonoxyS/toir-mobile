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
            respondJson("""
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
            """.trimIndent())
        }

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess, "Push failed: $result")
        val syncResult = result.getOrThrow()
        assertEquals(3, syncResult.acceptedCount)
        assertEquals(0, syncResult.rejectedCount)

        val inspection = db.inspectionQueries.selectById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.SYNCED, inspection.sync_status)
    }
}
