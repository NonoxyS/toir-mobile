@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.sync.data.applier.ConfigChangesApplier
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestSyncApi.Companion.respondJson
import ru.mirea.toir.sync.fixtures.TestTokenStorage
import ru.mirea.toir.sync.fixtures.testDispatchers

class SyncRepositoryFetchTest {

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
    fun `fetchAndApplyDeltaChanges - applies one location and persists serverTime`() = runTest {
        syncApi.stubFetch {
            respondJson("""
                {
                    "assignments":[],"routes":[],"routePoints":[],"equipment":[],
                    "locations":[{"id":"loc-fetched","code":"L1","name":"Fetched Location","description":null,"parentLocationId":null,"updatedAt":"2026-05-13T12:00:00Z"}],
                    "checklists":[],"checklistItems":[],
                    "deletedIds":{"assignments":[],"routes":[],"routePoints":[],"equipment":[],"locations":[],"checklists":[],"checklistItems":[]},
                    "serverTime":"2026-05-13T12:00:00Z"
                }
            """.trimIndent())
        }

        val result = repo.fetchAndApplyDeltaChanges()

        assertTrue(result.isSuccess, "Fetch failed: $result")
        assertEquals(1, db.locationQueries.selectAll().executeAsList().size)
        val lastSync = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME).executeAsOneOrNull()
        assertEquals("2026-05-13T12:00:00Z", lastSync)
    }

    @Test
    fun `fetchAndApplyDeltaChanges HTTP 500 - returns failure - last_sync_time unchanged`() = runTest {
        val beforeValue = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME).executeAsOneOrNull()

        syncApi.stubFetch {
            with(TestSyncApi) { respondJson("server down", io.ktor.http.HttpStatusCode.InternalServerError) }
        }

        val result = repo.fetchAndApplyDeltaChanges()

        assertTrue(result.isFailure, "Expected failure but was $result")

        val afterValue = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME).executeAsOneOrNull()
        assertEquals(beforeValue, afterValue, "last_sync_time should NOT change after fetch failure")
    }
}
