@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
import ru.mirea.toir.sync.fixtures.TestData.seedPendingPhoto
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestSyncApi.Companion.respondJson
import ru.mirea.toir.sync.fixtures.TestTokenStorage
import ru.mirea.toir.sync.fixtures.deleteFakeFile
import ru.mirea.toir.sync.fixtures.testDispatchers
import ru.mirea.toir.sync.fixtures.writeFakeFile

class SyncRepositoryPhotoTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val syncApi = TestSyncApi()
    private val dispatchers = testDispatchers()

    // writeFakeFile returns a filesystem path; readFileBytes uses NSURL(string=), so we need
    // a "file://" URI. NSTemporaryDirectory returns a path like "/private/var/folders/.../",
    // so we prefix with "file://" to form a valid file URI.
    private lateinit var photoPath: String
    private lateinit var photoFileUri: String

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

    @BeforeTest
    fun setUp() {
        photoPath = writeFakeFile(ByteArray(100) { it.toByte() }, "test-photo-${TestData.PHOTO_ID}.jpg")
        photoFileUri = "file://$photoPath"
    }

    @AfterTest
    fun tearDown() {
        driver.close()
        deleteFakeFile(photoPath)
    }

    @Test
    fun `uploadPendingPhotos - happy - photo marked SYNCED with storageKey`() = runTest {
        db.seedFullPendingScenario()
        db.seedPendingPhoto(fileUri = photoFileUri)

        syncApi.stubPhoto {
            respondJson("""{"photoId":"${TestData.PHOTO_ID}","uploadedAt":"2026-05-13T12:00:00Z","storageKey":"s3://bucket/photo-1.jpg"}""")
        }

        val result = repo.uploadPendingPhotos()

        assertTrue(result.isSuccess, "Upload failed: $result")
        assertEquals(1L, result.getOrThrow())

        val photo = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID).executeAsOne()
        assertEquals(LocalSyncStatus.SYNCED, photo.sync_status)
        assertEquals("s3://bucket/photo-1.jpg", photo.storage_key)
    }

    @Test
    fun `uploadPendingPhotos HTTP 500 - attempt count incremented - stays PENDING`() = runTest {
        db.seedFullPendingScenario()
        db.seedPendingPhoto(fileUri = photoFileUri)

        syncApi.stubPhoto {
            with(TestSyncApi) { respondJson("server down", HttpStatusCode.InternalServerError) }
        }

        val result = repo.uploadPendingPhotos()

        // uploadPendingPhotos catches per-photo failures and continues; final Result is success(0)
        assertTrue(result.isSuccess, "Expected success(0) but was $result")
        assertEquals(0L, result.getOrThrow())

        val photo = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, photo.sync_status)
        assertEquals(1L, photo.sync_attempt_count)
    }
}
