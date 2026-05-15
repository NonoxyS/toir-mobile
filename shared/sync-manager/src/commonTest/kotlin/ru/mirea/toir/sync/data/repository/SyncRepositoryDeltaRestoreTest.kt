@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.repository

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.sync.data.applier.ConfigChangesApplier
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.fixtures.TestData
import ru.mirea.toir.sync.fixtures.TestData.seedAssignment
import ru.mirea.toir.sync.fixtures.TestData.seedChecklist
import ru.mirea.toir.sync.fixtures.TestData.seedChecklistItem
import ru.mirea.toir.sync.fixtures.TestData.seedEquipment
import ru.mirea.toir.sync.fixtures.TestData.seedLocation
import ru.mirea.toir.sync.fixtures.TestData.seedPendingChecklistItemResult
import ru.mirea.toir.sync.fixtures.TestData.seedPendingEquipmentResult
import ru.mirea.toir.sync.fixtures.TestData.seedPendingInspection
import ru.mirea.toir.sync.fixtures.TestData.seedRoute
import ru.mirea.toir.sync.fixtures.TestData.seedRoutePoint
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestPhotoFileWriter
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestSyncApi.Companion.respondJson
import ru.mirea.toir.sync.fixtures.TestTokenStorage
import ru.mirea.toir.sync.fixtures.testDispatchers

/**
 * Waypoint 11 Phase 6 Task 6.4 — end-to-end интеграционный сценарий «кнопка Обновить»:
 *  HTTP-ответ delta → applier → photo download. Покрывает закрытие цикла offline-first.
 *
 *  Сценарий 1: «Локальная БД пустая → delta-ответ с восстановлением → downloadMissingPhotos».
 *    Проверяет: после `fetchAndApplyDeltaChanges()` локально есть inspection/IER/CIR/photo
 *    (sync_status = synced, file_uri = null), затем `downloadMissingPhotos()` дописывает
 *    file_uri через `TestPhotoFileWriter` с правильными байтами (`photo-bytes-for-{id}`).
 *
 *  Сценарий 2: «Локальный pending CIR + delta-ответ → локальное не затёрто».
 *    Проверяет: даже после `fetchAndApplyDeltaChanges()` локальный комментарий юзера
 *    сохраняется (правило мёржа §1.3 в `upsertFromServer` SQL).
 */
class SyncRepositoryDeltaRestoreTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val syncApi = TestSyncApi()
    private val dispatchers = testDispatchers()
    private val photoFileWriter = TestPhotoFileWriter()
    private val inspectionStorage = InspectionStorageImpl(db, dispatchers)

    private val repo: SyncRepository = SyncRepositoryImpl(
        syncApiClient = syncApi.build(),
        inspectionStorage = inspectionStorage,
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

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun `delta restore happy path — fetch then downloadMissingPhotos writes file_uri`() = runTest {
        // Локальный конфиг есть (assignment/route/route point/checklist), пользовательских
        // данных нет — симулируем состояние «после переустановки» с уже скачанным конфигом.
        seedConfigSkeleton()

        syncApi.stubFetch { respondJson(restoredTreeDeltaJson()) }

        // Шаг 1: delta-ответ с восстановлением.
        val deltaResult = repo.fetchAndApplyDeltaChanges()
        assertTrue(deltaResult.isSuccess, "fetchAndApplyDeltaChanges failed: $deltaResult")

        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection, "inspection should be restored")
        assertEquals(LocalInspectionStatus.IN_PROGRESS, inspection.status)
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)

        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier)
        assertEquals(LocalEquipmentResultStatus.IN_PROGRESS, ier.status)

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir)
        assertEquals("server comment", cir.comment)

        val photoBefore = db.photoQueries.selectByChecklistItemResultId(
            TestData.CHECKLIST_ITEM_RESULT_ID,
        ).executeAsList().single()
        assertEquals(TestData.PHOTO_ID, photoBefore.id)
        assertNull(photoBefore.file_uri, "file_uri must be null until photo is downloaded")

        // KEY_LAST_SYNC_TIME продвинулся.
        val lastSync = db.syncMetaQueries.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME)
            .executeAsOneOrNull()
        assertEquals("2026-05-15T12:00:00Z", lastSync)

        // Шаг 2: downloadMissingPhotos дотягивает бинарь по `GET /photos/{id}`.
        val downloadResult = repo.downloadMissingPhotos()
        assertTrue(downloadResult.isSuccess, "downloadMissingPhotos failed: $downloadResult")
        assertEquals(1L, downloadResult.getOrThrow(), "exactly one photo expected to be downloaded")

        val photoAfter = db.photoQueries.selectByChecklistItemResultId(
            TestData.CHECKLIST_ITEM_RESULT_ID,
        ).executeAsList().single()
        assertEquals(
            "file:///test/photos/${TestData.PHOTO_ID}.jpg",
            photoAfter.file_uri,
            "file_uri must be set by TestPhotoFileWriter",
        )
        assertEquals(
            "photo-bytes-for-${TestData.PHOTO_ID}",
            photoFileWriter.writes[TestData.PHOTO_ID]?.bytes?.decodeToString(),
            "writer must receive the bytes returned by the stub photo-download endpoint",
        )
    }

    @Test
    fun `delta restore — local pending CIR is preserved end-to-end`() = runTest {
        seedConfigSkeleton()
        // Локальный pending tree с правкой пользователя.
        db.seedPendingInspection(syncStatus = LocalSyncStatus.PENDING)
        db.seedPendingEquipmentResult(syncStatus = LocalSyncStatus.PENDING)
        db.seedPendingChecklistItemResult(
            comment = "user comment",
            syncStatus = LocalSyncStatus.PENDING,
        )

        // Серверная копия CIR с другим комментарием.
        syncApi.stubFetch { respondJson(restoredTreeDeltaJson()) }

        val result = repo.fetchAndApplyDeltaChanges()
        assertTrue(result.isSuccess, "fetchAndApplyDeltaChanges failed: $result")

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir, "local CIR should still exist")
        assertEquals(
            "user comment",
            cir.comment,
            "delta must not overwrite pending local row (Waypoint 11 §1.3)",
        )
        assertEquals(LocalSyncStatus.PENDING, cir.syncStatus)
    }

    private fun seedConfigSkeleton() {
        db.seedLocation()
        db.seedEquipment()
        db.seedChecklist()
        db.seedChecklistItem()
        db.seedRoute()
        db.seedRoutePoint()
        db.seedAssignment()
    }

    /**
     * JSON-копия `restoredTreeResponse` из applier-теста, но через wire-формат — чтобы убедиться,
     * что `RemoteConfigChangesResponse` (Task 6.1) реально десериализуется и проходит до applier.
     */
    private fun restoredTreeDeltaJson(): String = """
        {
            "assignments":[],"routes":[],"routePoints":[],"equipment":[],
            "locations":[],"checklists":[],"checklistItems":[],
            "inspections":[
                {
                    "id":"${TestData.INSPECTION_ID}",
                    "routeAssignmentId":"${TestData.ASSIGNMENT_ID}",
                    "routeId":"${TestData.ROUTE_ID}",
                    "status":"in_progress",
                    "startedAt":"${TestData.NOW}",
                    "completedAt":null,
                    "createdAt":"${TestData.NOW}",
                    "updatedAt":"${TestData.NOW}"
                }
            ],
            "inspectionEquipmentResults":[
                {
                    "id":"${TestData.EQUIPMENT_RESULT_ID}",
                    "inspectionId":"${TestData.INSPECTION_ID}",
                    "equipmentId":"${TestData.EQUIPMENT_ID}",
                    "routePointId":"${TestData.ROUTE_POINT_ID}",
                    "status":"in_progress",
                    "startedAt":"${TestData.NOW}",
                    "completedAt":null,
                    "createdAt":"${TestData.NOW}",
                    "updatedAt":"${TestData.NOW}"
                }
            ],
            "checklistItemResults":[
                {
                    "id":"${TestData.CHECKLIST_ITEM_RESULT_ID}",
                    "inspectionEquipmentResultId":"${TestData.EQUIPMENT_RESULT_ID}",
                    "checklistItemId":"${TestData.CHECKLIST_ITEM_ID}",
                    "valueBoolean":true,
                    "comment":"server comment",
                    "createdAt":"${TestData.NOW}",
                    "updatedAt":"${TestData.NOW}"
                }
            ],
            "photos":[
                {
                    "id":"${TestData.PHOTO_ID}",
                    "checklistItemResultId":"${TestData.CHECKLIST_ITEM_RESULT_ID}",
                    "fileName":"server-photo.jpg",
                    "mimeType":"image/jpeg",
                    "sizeBytes":1024,
                    "checksum":null,
                    "createdAt":"${TestData.NOW}",
                    "uploadedAt":"${TestData.NOW}"
                }
            ],
            "deletedIds":{
                "assignments":[],"routes":[],"routePoints":[],"equipment":[],
                "locations":[],"checklists":[],"checklistItems":[]
            },
            "serverTime":"2026-05-15T12:00:00Z"
        }
    """.trimIndent()
}
