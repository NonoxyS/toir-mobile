package ru.mirea.toir.core.database.storage.inspection

import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection

internal class InspectionStorageImpl(db: ToirDatabase) : InspectionStorage {

    private val inspectionQueries = db.inspectionQueries
    private val equipmentResultQueries = db.inspectionEquipmentResultQueries
    private val checklistItemResultQueries = db.checklistItemResultQueries

    override fun insertInspection(
        id: String,
        assignmentId: String,
        routeId: String,
        status: LocalRouteStatus,
        startedAt: String,
    ) {
        inspectionQueries.insertInspection(
            id = id,
            assignment_id = assignmentId,
            route_id = routeId,
            status = status,
            started_at = startedAt,
            completed_at = null,
            sync_status = LocalSyncStatus.PENDING,
        )
    }

    override fun selectInspectionByAssignmentId(assignmentId: String): LocalInspection? =
        inspectionQueries.selectByAssignmentId(assignmentId).executeAsOneOrNull()?.toLocal()

    override fun selectInspectionById(id: String): LocalInspection? =
        inspectionQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun updateInspectionStatus(id: String, status: LocalRouteStatus, completedAt: String?) {
        inspectionQueries.updateStatus(status = status, completed_at = completedAt, id = id)
    }

    override fun selectPendingInspections(): List<LocalInspection> =
        inspectionQueries.selectPending().executeAsList().map { it.toLocal() }

    override fun updateInspectionSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        inspectionQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    override fun insertEquipmentResult(
        id: String,
        inspectionId: String,
        routePointId: String,
        equipmentId: String,
        status: LocalEquipmentResultStatus,
    ) {
        equipmentResultQueries.insertResult(
            id = id,
            inspection_id = inspectionId,
            route_point_id = routePointId,
            equipment_id = equipmentId,
            status = status,
            started_at = null,
            completed_at = null,
            sync_status = LocalSyncStatus.PENDING,
        )
    }

    override fun selectEquipmentResultsByInspectionId(inspectionId: String): List<LocalEquipmentResult> =
        equipmentResultQueries.selectByInspectionId(inspectionId).executeAsList().map { it.toLocal() }

    override fun selectEquipmentResultById(id: String): LocalEquipmentResult? =
        equipmentResultQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun selectEquipmentResultByRoutePoint(
        routePointId: String,
        inspectionId: String,
    ): LocalEquipmentResult? =
        equipmentResultQueries.selectByRoutePointAndInspection(routePointId, inspectionId)
            .executeAsOneOrNull()?.toLocal()

    override fun updateEquipmentResultStatus(
        id: String,
        status: LocalEquipmentResultStatus,
        startedAt: String?,
        completedAt: String?,
    ) {
        equipmentResultQueries.updateStatus(
            status = status,
            started_at = startedAt,
            completed_at = completedAt,
            id = id,
        )
    }

    override fun selectPendingEquipmentResults(): List<LocalEquipmentResult> =
        equipmentResultQueries.selectPending().executeAsList().map { it.toLocal() }

    override fun updateEquipmentResultSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        equipmentResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    override fun insertOrReplaceChecklistItemResult(
        id: String,
        equipmentResultId: String,
        checklistItemId: String,
        valueBoolean: Long?,
        valueNumber: Double?,
        valueText: String?,
        valueSelect: String?,
        isConfirmed: Long,
        answeredAt: String?,
    ) {
        checklistItemResultQueries.insertOrReplaceResult(
            id = id,
            inspection_equipment_result_id = equipmentResultId,
            checklist_item_id = checklistItemId,
            value_boolean = valueBoolean,
            value_number = valueNumber,
            value_text = valueText,
            value_select = valueSelect,
            is_confirmed = isConfirmed,
            answered_at = answeredAt,
            sync_status = LocalSyncStatus.PENDING,
        )
    }

    override fun selectChecklistItemResultsByEquipmentResult(
        equipmentResultId: String,
    ): List<LocalChecklistItemResult> =
        checklistItemResultQueries.selectByEquipmentResultId(equipmentResultId)
            .executeAsList().map { it.toLocal() }

    override fun selectChecklistItemResult(
        checklistItemId: String,
        equipmentResultId: String,
    ): LocalChecklistItemResult? =
        checklistItemResultQueries.selectByChecklistItemAndEquipmentResult(checklistItemId, equipmentResultId)
            .executeAsOneOrNull()?.toLocal()

    override fun selectPendingChecklistItemResults(): List<LocalChecklistItemResult> =
        checklistItemResultQueries.selectPending().executeAsList().map { it.toLocal() }

    override fun updateChecklistItemResultSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        checklistItemResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    private fun Inspections.toLocal() = LocalInspection(
        id = id,
        assignmentId = assignment_id,
        routeId = route_id,
        status = status,
        startedAt = started_at,
        completedAt = completed_at,
        syncStatus = sync_status,
    )

    private fun Inspection_equipment_results.toLocal() = LocalEquipmentResult(
        id = id,
        inspectionId = inspection_id,
        routePointId = route_point_id,
        equipmentId = equipment_id,
        status = status,
        startedAt = started_at,
        completedAt = completed_at,
        syncStatus = sync_status,
    )

    private fun Checklist_item_results.toLocal() = LocalChecklistItemResult(
        id = id,
        equipmentResultId = inspection_equipment_result_id,
        checklistItemId = checklist_item_id,
        valueBoolean = value_boolean,
        valueNumber = value_number,
        valueText = value_text,
        valueSelect = value_select,
        isConfirmed = is_confirmed,
        answeredAt = answered_at,
        syncStatus = sync_status,
    )
}
