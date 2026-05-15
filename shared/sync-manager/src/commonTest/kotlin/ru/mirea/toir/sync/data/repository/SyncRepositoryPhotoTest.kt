@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
import ru.mirea.toir.sync.fixtures.TestPhotoFileWriter
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
    private val photoFileWriter = TestPhotoFileWriter()

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
            inspectionStorage = InspectionStorageImpl(db, dispatchers),
            photoStorage = PhotoStorageImpl(db, dispatchers),
            transactionRunner = TransactionRunnerImpl(db),
        ),
        photoFileWriter = photoFileWriter,
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
            respondJson(
                """
                    {
                        "photoId":"${TestData.PHOTO_ID}",
                        "uploadedAt":"2026-05-13T12:00:00Z",
                        "storageKey":"s3://bucket/photo-1.jpg"
                    }
                """.trimIndent()
            )
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

    // ---------------- downloadMissingPhotos ----------------

    @Test
    fun `downloadMissingPhotos - happy path - two photos downloaded then idempotent`() = runTest {
        db.seedFullPendingScenario()
        // Two restored photos, both with file_uri = NULL (set by insertRestoredPhoto).
        db.photoQueries.insertRestoredPhoto(
            id = "restored-1",
            checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
            takenAt = TestData.NOW,
            fileName = "p1.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 100L,
            checksum = null,
        )
        db.photoQueries.insertRestoredPhoto(
            id = "restored-2",
            checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
            takenAt = TestData.NOW,
            fileName = "p2.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 100L,
            checksum = null,
        )

        // Default stub returns bytes derived from the photoId.
        val result = repo.downloadMissingPhotos()

        assertTrue(result.isSuccess, "Expected success but got $result")
        assertEquals(2L, result.getOrThrow())

        // Both rows now have file_uri pointing at the writer's synthetic path.
        val photos = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
            .executeAsList()
            .associateBy { it.id }
        assertEquals("file:///test/photos/restored-1.jpg", photos["restored-1"]?.file_uri)
        assertEquals("file:///test/photos/restored-2.jpg", photos["restored-2"]?.file_uri)

        // Writer received the right bytes for each id.
        assertEquals(
            "photo-bytes-for-restored-1",
            photoFileWriter.writes["restored-1"]?.bytes?.decodeToString(),
        )
        assertEquals(
            "photo-bytes-for-restored-2",
            photoFileWriter.writes["restored-2"]?.bytes?.decodeToString(),
        )

        // Idempotent: a second call has nothing to download.
        val second = repo.downloadMissingPhotos()
        assertTrue(second.isSuccess)
        assertEquals(0L, second.getOrThrow())
    }

    @Test
    fun `downloadMissingPhotos - per-photo failure - continues with the rest`() = runTest {
        db.seedFullPendingScenario()
        db.photoQueries.insertRestoredPhoto(
            id = "fail-1",
            checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
            takenAt = TestData.NOW,
            fileName = null,
            mimeType = null,
            sizeBytes = null,
            checksum = null,
        )
        db.photoQueries.insertRestoredPhoto(
            id = "ok-2",
            checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
            takenAt = TestData.NOW,
            fileName = null,
            mimeType = null,
            sizeBytes = null,
            checksum = null,
        )

        // First photo: HTTP 500. Second: default success bytes.
        syncApi.stubPhotoDownload { request ->
            val id = request.url.encodedPath.substringAfterLast('/')
            if (id == "fail-1") {
                respond("server down", HttpStatusCode.InternalServerError)
            } else {
                respond(
                    content = "photo-bytes-for-$id".encodeToByteArray(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "image/jpeg"),
                )
            }
        }

        val result = repo.downloadMissingPhotos()

        // Step succeeds overall; failed photo logged and skipped.
        assertTrue(result.isSuccess, "Expected success(1) but got $result")
        assertEquals(1L, result.getOrThrow())

        val photos = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
            .executeAsList()
            .associateBy { it.id }
        // Failed photo stays in selectMissingFiles (file_uri null).
        assertNull(photos["fail-1"]?.file_uri)
        // Successful photo got its uri.
        assertEquals("file:///test/photos/ok-2.jpg", photos["ok-2"]?.file_uri)

        // Failed photo will be retried on the next sync cycle.
        val missing = db.photoQueries.selectMissingFiles().executeAsList()
        assertEquals(listOf("fail-1"), missing.map { it.id })
    }

    @Test
    fun `downloadMissingPhotos - already-downloaded photo is not re-downloaded`() = runTest {
        db.seedFullPendingScenario()
        // A photo that already has a file_uri (e.g. captured locally or previously downloaded)
        // must not appear in selectMissingFiles, hence must not be downloaded again.
        db.seedPendingPhoto(
            id = "have-file-1",
            fileUri = photoFileUri,
            syncStatus = LocalSyncStatus.SYNCED,
        )
        // A photo with NULL file_uri DOES need download.
        db.photoQueries.insertRestoredPhoto(
            id = "needs-file-2",
            checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
            takenAt = TestData.NOW,
            fileName = null,
            mimeType = null,
            sizeBytes = null,
            checksum = null,
        )

        val result = repo.downloadMissingPhotos()

        assertTrue(result.isSuccess, "Expected success(1) but got $result")
        assertEquals(1L, result.getOrThrow())

        // Writer was called for the missing one only.
        assertTrue(photoFileWriter.writes.containsKey("needs-file-2"))
        assertTrue(!photoFileWriter.writes.containsKey("have-file-1"))

        // The already-present file_uri is untouched.
        val untouched = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
            .executeAsList()
            .first { it.id == "have-file-1" }
        assertEquals(photoFileUri, untouched.file_uri)
    }
}
