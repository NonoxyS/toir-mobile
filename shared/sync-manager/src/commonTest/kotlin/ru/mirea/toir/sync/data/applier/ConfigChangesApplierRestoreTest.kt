@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.applier

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesChecklistItemResult
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesEquipmentResult
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesInspection
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesPhoto
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
import ru.mirea.toir.sync.data.network.models.RemoteDeletedIds
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
import ru.mirea.toir.sync.fixtures.testDispatchers

/**
 * Delta restore через applier:
 *  1. Пустая локальная БД → восстановленное дерево записывается с sync_status = synced.
 *  2. Локальный pending CIR с правкой пользователя → серверная копия НЕ затирает.
 */
internal class ConfigChangesApplierRestoreTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val dispatchers = testDispatchers()
    private val inspectionStorage = InspectionStorageImpl(db, dispatchers)
    private val photoStorage = PhotoStorageImpl(db, dispatchers)

    private val applier = ConfigChangesApplier(
        routeStorage = RouteStorageImpl(db, dispatchers),
        equipmentStorage = EquipmentStorageImpl(db, dispatchers),
        locationStorage = LocationStorageImpl(db),
        checklistStorage = ChecklistStorageImpl(db, dispatchers),
        inspectionStorage = inspectionStorage,
        photoStorage = photoStorage,
        transactionRunner = TransactionRunnerImpl(db),
    )

    @AfterTest
    fun tearDown() = driver.close()

    /**
     * Scenario 1 — wipe + delta. Локальная БД содержит только конфиг-скелет (assignment, route,
     * route point, checklist), но нет inspection / IER / CIR / photo. Delta-ответ приносит
     * восстановление. После apply всё лежит локально с sync_status = synced.
     */
    @Test
    fun `delta restore — empty local tree is inserted with sync_status synced`() {
        seedConfigSkeleton()

        applier.apply(restoredTreeResponse())

        val inspection = inspectionStorage.selectInspectionById(TestData.INSPECTION_ID)
        assertNotNull(inspection, "inspection should be restored")
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)
        assertEquals(LocalInspectionStatus.IN_PROGRESS, inspection.status)

        val ier = inspectionStorage.selectEquipmentResultById(TestData.EQUIPMENT_RESULT_ID)
        assertNotNull(ier, "equipment result should be restored")
        assertEquals(LocalSyncStatus.SYNCED, ier.syncStatus)
        assertEquals(LocalEquipmentResultStatus.IN_PROGRESS, ier.status)

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir, "checklist item result should be restored")
        assertEquals(LocalSyncStatus.SYNCED, cir.syncStatus)
        assertEquals("server comment", cir.comment)

        val photos = db.photoQueries.selectByChecklistItemResultId(TestData.CHECKLIST_ITEM_RESULT_ID)
            .executeAsList()
        assertEquals(1, photos.size)
        assertEquals(TestData.PHOTO_ID, photos[0].id)
        assertEquals("server-photo.jpg", photos[0].file_name)
        assertEquals(LocalSyncStatus.SYNCED, photos[0].sync_status)
        // file_uri = null — фото докачивается асинхронно через downloadMissingPhotos.
        assertEquals(null, photos[0].file_uri)
    }

    /**
     * Scenario 2 — локальный pending CIR с правкой пользователя. Например, во время сессии
     * юзер дописал комментарий, но push ещё не прошёл. Параллельно прилетела delta с серверной
     * копией того же CIR (старое значение комментария — "server comment"). После apply
     * локальная запись НЕ затёрта: `comment = "user comment"`, `sync_status = pending`.
     *
     * Реализует ВКР п.6 «Разрешение конфликтов» через SQL-merge §1.3.
     */
    @Test
    fun `delta restore — local pending CIR is NOT clobbered by server copy`() {
        seedConfigSkeleton()
        // Локальное дерево с pending CIR и комментарием юзера.
        db.seedPendingInspection(syncStatus = LocalSyncStatus.PENDING)
        db.seedPendingEquipmentResult(syncStatus = LocalSyncStatus.PENDING)
        db.seedPendingChecklistItemResult(
            comment = "user comment",
            syncStatus = LocalSyncStatus.PENDING,
        )

        applier.apply(restoredTreeResponse())

        val cir = inspectionStorage.selectChecklistItemResult(
            checklistItemId = TestData.CHECKLIST_ITEM_ID,
            equipmentResultId = TestData.EQUIPMENT_RESULT_ID,
        )
        assertNotNull(cir, "local CIR should still exist")
        assertEquals(
            "user comment",
            cir.comment,
            "local pending row must not be overwritten by server copy",
        )
        assertEquals(
            LocalSyncStatus.PENDING,
            cir.syncStatus,
            "local sync_status must remain pending",
        )
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

    private fun restoredTreeResponse(): RemoteConfigChangesResponse = RemoteConfigChangesResponse(
        // Конфиг-часть пустая — мы тестируем именно восстановительные поля.
        // deletedIds + serverTime обязательны.
        deletedIds = RemoteDeletedIds(),
        serverTime = "2026-05-15T12:00:00Z",
        inspections = listOf(
            RemoteConfigChangesInspection(
                id = TestData.INSPECTION_ID,
                routeAssignmentId = TestData.ASSIGNMENT_ID,
                routeId = TestData.ROUTE_ID,
                status = "in_progress",
                startedAt = TestData.NOW,
                completedAt = null,
                createdAt = TestData.NOW,
                updatedAt = TestData.NOW,
            ),
        ),
        inspectionEquipmentResults = listOf(
            RemoteConfigChangesEquipmentResult(
                id = TestData.EQUIPMENT_RESULT_ID,
                inspectionId = TestData.INSPECTION_ID,
                equipmentId = TestData.EQUIPMENT_ID,
                routePointId = TestData.ROUTE_POINT_ID,
                status = "in_progress",
                startedAt = TestData.NOW,
                completedAt = null,
                createdAt = TestData.NOW,
                updatedAt = TestData.NOW,
            ),
        ),
        checklistItemResults = listOf(
            RemoteConfigChangesChecklistItemResult(
                id = TestData.CHECKLIST_ITEM_RESULT_ID,
                inspectionEquipmentResultId = TestData.EQUIPMENT_RESULT_ID,
                checklistItemId = TestData.CHECKLIST_ITEM_ID,
                valueBoolean = true,
                comment = "server comment",
                createdAt = TestData.NOW,
                updatedAt = TestData.NOW,
            ),
        ),
        photos = listOf(
            RemoteConfigChangesPhoto(
                id = TestData.PHOTO_ID,
                checklistItemResultId = TestData.CHECKLIST_ITEM_RESULT_ID,
                fileName = "server-photo.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 1024L,
                checksum = null,
                createdAt = TestData.NOW,
                uploadedAt = TestData.NOW,
            ),
        ),
    )
}
