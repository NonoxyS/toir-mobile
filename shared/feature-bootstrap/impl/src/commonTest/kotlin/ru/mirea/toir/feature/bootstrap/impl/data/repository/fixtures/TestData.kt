@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapChecklistItemResult
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapEquipmentResult
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapInspection
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapPhoto
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

/**
 * Seeders and canned-response builders used by `BootstrapRepositoryImplMergeTest`.
 * Mirrors `sync-manager:commonTest/fixtures/TestData.kt` but trimmed to what
 * bootstrap restore needs (no action_logs / sync_batches).
 */
internal object TestData {

    const val LOCATION_ID = "loc-1"
    const val EQUIPMENT_ID = "eq-1"
    const val ROUTE_ID = "route-1"
    const val ROUTE_POINT_ID = "rp-1"
    const val ASSIGNMENT_ID = "asg-1"
    const val CHECKLIST_ID = "chk-1"
    const val CHECKLIST_ITEM_ID = "ci-1"
    const val INSPECTION_ID = "ins-1"
    const val EQUIPMENT_RESULT_ID = "eqr-1"
    const val CHECKLIST_ITEM_RESULT_ID = "cir-1"
    const val PHOTO_ID = "photo-1"
    const val EQUIPMENT_TYPE = "pump"
    const val USER_ID = "user-1"
    const val NOW = "2026-05-15T12:00:00Z"
    const val EARLIER = "2026-05-15T10:00:00Z"

    fun ToirDatabase.seedConfigSkeleton() {
        locationQueries.upsertLocation(
            id = LOCATION_ID,
            code = "LOC-1",
            name = "L1",
            description = null,
            parent_location_id = null,
        )
        equipmentQueries.upsertEquipment(
            id = EQUIPMENT_ID,
            code = "EQ-1",
            name = "E1",
            type = EQUIPMENT_TYPE,
            location_id = LOCATION_ID,
            qr_code = null,
        )
        checklistQueries.upsertChecklist(
            id = CHECKLIST_ID,
            code = "CHK-1",
            name = "Checklist 1",
            equipment_type = EQUIPMENT_TYPE,
            description = null,
        )
        checklistItemQueries.upsertChecklistItem(
            id = CHECKLIST_ITEM_ID,
            checklist_id = CHECKLIST_ID,
            title = "Q1",
            description = null,
            answer_type = "boolean",
            is_required = 1L,
            requires_photo = 0L,
            select_options = null,
            numeric_min = null,
            numeric_max = null,
            order_index = 0L,
        )
        routeQueries.upsertRoute(
            id = ROUTE_ID,
            code = "RT-1",
            name = "Route 1",
            description = null,
        )
        routePointQueries.upsertRoutePoint(
            id = ROUTE_POINT_ID,
            route_id = ROUTE_ID,
            equipment_id = EQUIPMENT_ID,
            checklist_id = CHECKLIST_ID,
            order_index = 0L,
        )
        routeAssignmentQueries.upsertRouteAssignment(
            id = ASSIGNMENT_ID,
            route_id = ROUTE_ID,
            user_id = USER_ID,
            status = LocalRouteAssignmentStatus.IN_PROGRESS,
            assigned_at = NOW,
            shift_code = null,
            updated_at = NOW,
        )
    }

    fun ToirDatabase.seedLocalInspection(
        id: String = INSPECTION_ID,
        assignmentId: String? = ASSIGNMENT_ID,
        routeId: String = ROUTE_ID,
        status: LocalInspectionStatus = LocalInspectionStatus.IN_PROGRESS,
        startedAt: String? = NOW,
        completedAt: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
        syncStatus: LocalSyncStatus = LocalSyncStatus.PENDING,
    ) {
        inspectionQueries.insertInspection(
            id = id,
            assignment_id = assignmentId,
            route_id = routeId,
            status = status,
            started_at = startedAt,
            completed_at = completedAt,
            created_at = createdAt,
            updated_at = updatedAt,
            sync_status = syncStatus,
        )
    }

    fun ToirDatabase.seedLocalEquipmentResult(
        id: String = EQUIPMENT_RESULT_ID,
        inspectionId: String = INSPECTION_ID,
        routePointId: String = ROUTE_POINT_ID,
        equipmentId: String = EQUIPMENT_ID,
        status: LocalEquipmentResultStatus = LocalEquipmentResultStatus.IN_PROGRESS,
        startedAt: String? = NOW,
        completedAt: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
        syncStatus: LocalSyncStatus = LocalSyncStatus.PENDING,
    ) {
        inspectionEquipmentResultQueries.insertResult(
            id = id,
            inspection_id = inspectionId,
            route_point_id = routePointId,
            equipment_id = equipmentId,
            status = status,
            started_at = startedAt,
            completed_at = completedAt,
            created_at = createdAt,
            updated_at = updatedAt,
            sync_status = syncStatus,
        )
    }

    fun ToirDatabase.seedLocalChecklistItemResult(
        id: String = CHECKLIST_ITEM_RESULT_ID,
        equipmentResultId: String = EQUIPMENT_RESULT_ID,
        checklistItemId: String = CHECKLIST_ITEM_ID,
        valueBoolean: Long? = 1L,
        valueNumber: Double? = null,
        valueText: String? = null,
        selectedOption: String? = null,
        comment: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
        syncStatus: LocalSyncStatus = LocalSyncStatus.PENDING,
    ) {
        checklistItemResultQueries.insertOrReplaceResult(
            id = id,
            inspection_equipment_result_id = equipmentResultId,
            checklist_item_id = checklistItemId,
            value_boolean = valueBoolean,
            value_number = valueNumber,
            value_text = valueText,
            selected_option = selectedOption,
            comment = comment,
            created_at = createdAt,
            updated_at = updatedAt,
            sync_status = syncStatus,
        )
    }

    /** Server-side copy of the inspection — used both as the canned response body
     *  and as the "what the server thinks" check value in tests. */
    fun remoteInspection(
        id: String = INSPECTION_ID,
        status: String = "in_progress",
        startedAt: String? = NOW,
        completedAt: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
    ) = RemoteBootstrapInspection(
        id = id,
        routeAssignmentId = ASSIGNMENT_ID,
        routeId = ROUTE_ID,
        status = status,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun remoteEquipmentResult(
        id: String = EQUIPMENT_RESULT_ID,
        inspectionId: String = INSPECTION_ID,
        status: String = "in_progress",
        startedAt: String? = NOW,
        completedAt: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
    ) = RemoteBootstrapEquipmentResult(
        id = id,
        inspectionId = inspectionId,
        equipmentId = EQUIPMENT_ID,
        routePointId = ROUTE_POINT_ID,
        status = status,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun remoteChecklistItemResult(
        id: String = CHECKLIST_ITEM_RESULT_ID,
        equipmentResultId: String = EQUIPMENT_RESULT_ID,
        valueBoolean: Boolean? = true,
        valueNumber: Double? = null,
        valueText: String? = null,
        comment: String? = null,
        createdAt: String = NOW,
        updatedAt: String = NOW,
    ) = RemoteBootstrapChecklistItemResult(
        id = id,
        inspectionEquipmentResultId = equipmentResultId,
        checklistItemId = CHECKLIST_ITEM_ID,
        valueText = valueText,
        valueNumber = valueNumber,
        valueBoolean = valueBoolean,
        selectedOption = null,
        comment = comment,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun remotePhoto(
        id: String = PHOTO_ID,
        checklistItemResultId: String = CHECKLIST_ITEM_RESULT_ID,
        fileName: String = "photo.jpg",
        mimeType: String = "image/jpeg",
        sizeBytes: Long = 1024L,
        checksum: String? = "sha256:abc",
        createdAt: String = NOW,
        uploadedAt: String? = NOW,
    ) = RemoteBootstrapPhoto(
        id = id,
        checklistItemResultId = checklistItemResultId,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        checksum = checksum,
        createdAt = createdAt,
        uploadedAt = uploadedAt,
    )

    /**
     * Bootstrap response carrying a single restored inspection / IER / CIR / photo,
     * plus zero config rows (test seeds config separately). `serverTime` is fixed
     * because syncMeta isn't load-bearing for merge assertions.
     */
    fun bootstrapResponseWithSingleRestoredTree(): RemoteBootstrapResponse =
        RemoteBootstrapResponse(
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
            serverTime = NOW,
        )
}
