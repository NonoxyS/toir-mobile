package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus

object TestData {

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
    const val NOW = "2026-05-13T12:00:00Z"

    fun ToirDatabase.seedLocation(
        id: String = LOCATION_ID,
        code: String = "LOC-1",
        name: String = "L1",
        description: String? = null,
        parentLocationId: String? = null,
    ) {
        locationQueries.upsertLocation(
            id = id,
            code = code,
            name = name,
            description = description,
            parent_location_id = parentLocationId,
        )
    }

    fun ToirDatabase.seedEquipment(
        id: String = EQUIPMENT_ID,
        code: String = "EQ-1",
        name: String = "Equipment 1",
        type: String = "pump",
        locationId: String? = LOCATION_ID,
        qrCode: String? = null,
    ) {
        equipmentQueries.upsertEquipment(
            id = id,
            code = code,
            name = name,
            type = type,
            location_id = locationId,
            qr_code = qrCode,
        )
    }

    fun ToirDatabase.seedChecklist(
        id: String = CHECKLIST_ID,
        code: String = "CHK-1",
        name: String = "Checklist 1",
        equipmentType: String = "pump",
        description: String? = null,
    ) {
        checklistQueries.upsertChecklist(
            id = id,
            code = code,
            name = name,
            equipment_type = equipmentType,
            description = description,
        )
    }

    fun ToirDatabase.seedChecklistItem(
        id: String = CHECKLIST_ITEM_ID,
        checklistId: String = CHECKLIST_ID,
        title: String = "Check item 1",
        description: String? = null,
        answerType: String = "boolean",
        isRequired: Long = 1L,
        requiresPhoto: Long = 0L,
        selectOptions: String? = null,
        numericMin: Double? = null,
        numericMax: Double? = null,
        orderIndex: Long = 0L,
    ) {
        checklistItemQueries.upsertChecklistItem(
            id = id,
            checklist_id = checklistId,
            title = title,
            description = description,
            answer_type = answerType,
            is_required = isRequired,
            requires_photo = requiresPhoto,
            select_options = selectOptions,
            numeric_min = numericMin,
            numeric_max = numericMax,
            order_index = orderIndex,
        )
    }

    fun ToirDatabase.seedRoute(
        id: String = ROUTE_ID,
        code: String = "RT-1",
        name: String = "Route 1",
        description: String? = null,
    ) {
        routeQueries.upsertRoute(
            id = id,
            code = code,
            name = name,
            description = description,
        )
    }

    fun ToirDatabase.seedRoutePoint(
        id: String = ROUTE_POINT_ID,
        routeId: String = ROUTE_ID,
        equipmentId: String = EQUIPMENT_ID,
        checklistId: String = CHECKLIST_ID,
        orderIndex: Long = 0L,
    ) {
        routePointQueries.upsertRoutePoint(
            id = id,
            route_id = routeId,
            equipment_id = equipmentId,
            checklist_id = checklistId,
            order_index = orderIndex,
        )
    }

    fun ToirDatabase.seedAssignment(
        id: String = ASSIGNMENT_ID,
        routeId: String = ROUTE_ID,
        userId: String = "user-1",
        status: LocalRouteAssignmentStatus = LocalRouteAssignmentStatus.IN_PROGRESS,
        assignedAt: String = NOW,
        shiftCode: String? = null,
        updatedAt: String = NOW,
    ) {
        routeAssignmentQueries.upsertRouteAssignment(
            id = id,
            route_id = routeId,
            user_id = userId,
            status = status,
            assigned_at = assignedAt,
            shift_code = shiftCode,
            updated_at = updatedAt,
        )
    }

    fun ToirDatabase.seedPendingInspection(
        id: String = INSPECTION_ID,
        assignmentId: String? = ASSIGNMENT_ID,
        routeId: String = ROUTE_ID,
        status: LocalInspectionStatus = LocalInspectionStatus.COMPLETED,
        startedAt: String? = NOW,
        completedAt: String? = NOW,
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

    fun ToirDatabase.seedPendingEquipmentResult(
        id: String = EQUIPMENT_RESULT_ID,
        inspectionId: String = INSPECTION_ID,
        routePointId: String = ROUTE_POINT_ID,
        equipmentId: String = EQUIPMENT_ID,
        status: LocalEquipmentResultStatus = LocalEquipmentResultStatus.COMPLETED,
        startedAt: String? = NOW,
        completedAt: String? = NOW,
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

    fun ToirDatabase.seedPendingChecklistItemResult(
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

    fun ToirDatabase.seedPendingPhoto(
        id: String = PHOTO_ID,
        checklistItemResultId: String = CHECKLIST_ITEM_RESULT_ID,
        fileUri: String = "/tmp/photo.jpg",
        takenAt: String = NOW,
        syncStatus: LocalSyncStatus = LocalSyncStatus.PENDING,
        storageKey: String? = null,
    ) {
        photoQueries.insertPhoto(
            id = id,
            checklist_item_result_id = checklistItemResultId,
            file_uri = fileUri,
            taken_at = takenAt,
            sync_status = syncStatus,
            storage_key = storageKey,
        )
    }

    fun ToirDatabase.seedFullPendingScenario() {
        seedLocation()
        seedEquipment()
        seedChecklist()
        seedChecklistItem()
        seedRoute()
        seedRoutePoint()
        seedAssignment()
        seedPendingInspection()
        seedPendingEquipmentResult()
        seedPendingChecklistItemResult()
    }
}
