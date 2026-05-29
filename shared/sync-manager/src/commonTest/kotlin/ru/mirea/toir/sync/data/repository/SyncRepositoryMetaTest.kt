@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
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
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestFileReader
import ru.mirea.toir.sync.fixtures.TestPhotoFileWriter
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestTokenStorage
import ru.mirea.toir.sync.fixtures.testDispatchers

@OptIn(ExperimentalTime::class)
class SyncRepositoryMetaTest {

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
            inspectionStorage = InspectionStorageImpl(db, dispatchers),
            photoStorage = PhotoStorageImpl(db, dispatchers),
            transactionRunner = TransactionRunnerImpl(db),
        ),
        photoFileWriter = TestPhotoFileWriter(),
        fileReader = TestFileReader(),
        transactionRunner = TransactionRunnerImpl(db),
        coroutineDispatchers = dispatchers,
    )

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `recordSuccessfulRun writes KEY_LAST_SYNC_AT_SUCCESS`() = runTest {
        val now = Instant.parse("2026-05-13T12:00:00Z")
        repo.recordSuccessfulRun(now)

        val stored = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_AT_SUCCESS).executeAsOneOrNull()
        assertEquals(now.toString(), stored)
    }

    @Test
    fun `recordFailedRun writes both error keys`() = runTest {
        val now = Instant.parse("2026-05-13T12:05:00Z")
        repo.recordFailedRun(now, SyncFailureReason.NETWORK)

        val ts = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_ERROR_AT).executeAsOneOrNull()
        val reason = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_ERROR_REASON).executeAsOneOrNull()

        assertEquals(now.toString(), ts)
        assertEquals("NETWORK", reason)
    }
}
