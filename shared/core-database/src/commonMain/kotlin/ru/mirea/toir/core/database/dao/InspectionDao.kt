package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus

class InspectionDao(db: ToirDatabase) {
    private val inspectionQueries = db.inspectionQueries
    private val equipmentResultQueries = db.inspectionEquipmentResultQueries
    private val checklistItemResultQueries = db.checklistItemResultQueries

    fun insertInspection(id: String, assignmentId: String, routeId: String, status: LocalRouteStatus, startedAt: String) {
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

    fun selectByAssignmentId(assignmentId: String): Inspections? =
        inspectionQueries.selectByAssignmentId(assignmentId).executeAsOneOrNull()

    fun selectById(id: String): Inspections? = inspectionQueries.selectById(id).executeAsOneOrNull()

    fun updateInspectionStatus(id: String, status: LocalRouteStatus, completedAt: String?) {
        inspectionQueries.updateStatus(status = status, completed_at = completedAt, id = id)
    }

    fun selectPendingInspections(): List<Inspections> =
        inspectionQueries.selectPending().executeAsList()

    fun updateInspectionSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        inspectionQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    @Suppress("LongParameterList")
    fun insertEquipmentResult(
        id: String,
        inspectionId: String,
        routePointId: String,
        equipmentId: String,
        status: String,
    ) {
        equipmentResultQueries.insertResult(
            id = id,
            inspection_id = inspectionId,
            route_point_id = routePointId,
            equipment_id = equipmentId,
            status = status,
            started_at = null,
            completed_at = null,
            sync_status = "PENDING",
        )
    }

    fun selectEquipmentResultsByInspectionId(inspectionId: String): List<Inspection_equipment_results> =
        equipmentResultQueries.selectByInspectionId(inspectionId).executeAsList()

    fun selectEquipmentResultById(id: String): Inspection_equipment_results? =
        equipmentResultQueries.selectById(id).executeAsOneOrNull()

    fun selectEquipmentResultByRoutePoint(routePointId: String, inspectionId: String): Inspection_equipment_results? =
        equipmentResultQueries.selectByRoutePointAndInspection(routePointId, inspectionId).executeAsOneOrNull()

    fun updateEquipmentResultStatus(id: String, status: String, startedAt: String?, completedAt: String?) {
        equipmentResultQueries.updateStatus(
            status = status,
            started_at = startedAt,
            completed_at = completedAt,
            id = id
        )
    }

    fun selectPendingEquipmentResults(): List<Inspection_equipment_results> =
        equipmentResultQueries.selectPending().executeAsList()

    fun updateEquipmentResultSyncStatus(id: String, syncStatus: String) {
        equipmentResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    fun insertOrReplaceChecklistItemResult(
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
            sync_status = "PENDING",
        )
    }

    fun selectChecklistItemResultsByEquipmentResult(equipmentResultId: String): List<Checklist_item_results> =
        checklistItemResultQueries.selectByEquipmentResultId(equipmentResultId).executeAsList()

    fun selectChecklistItemResult(checklistItemId: String, equipmentResultId: String): Checklist_item_results? =
        checklistItemResultQueries.selectByChecklistItemAndEquipmentResult(checklistItemId, equipmentResultId)
            .executeAsOneOrNull()

    fun selectPendingChecklistItemResults(): List<Checklist_item_results> =
        checklistItemResultQueries.selectPending().executeAsList()

    fun updateChecklistItemResultSyncStatus(id: String, syncStatus: String) {
        checklistItemResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }
}
