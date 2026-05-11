package ru.mirea.toir.core.database.storage.inspection

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection

internal class InspectionStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : InspectionStorage {

    private val inspectionQueries = db.inspectionQueries
    private val equipmentResultQueries = db.inspectionEquipmentResultQueries
    private val checklistItemResultQueries = db.checklistItemResultQueries

    override fun insertInspection(
        id: String,
        assignmentId: String?,
        routeId: String,
        status: LocalInspectionStatus,
        startedAt: String?,
        createdAt: String,
        updatedAt: String,
    ) {
        inspectionQueries.insertInspection(
            id = id,
            assignment_id = assignmentId,
            route_id = routeId,
            status = status,
            started_at = startedAt,
            completed_at = null,
            created_at = createdAt,
            updated_at = updatedAt,
            sync_status = LocalSyncStatus.PENDING,
        )
    }

    override fun selectInspectionByAssignmentId(assignmentId: String): LocalInspection? =
        inspectionQueries.selectByAssignmentId(assignmentId).executeAsOneOrNull()?.toLocal()

    override fun selectInspectionById(id: String): LocalInspection? =
        inspectionQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun updateInspectionStatus(
        id: String,
        status: LocalInspectionStatus,
        completedAt: String?,
        updatedAt: String,
    ) {
        inspectionQueries.updateStatus(
            status = status,
            completed_at = completedAt,
            updated_at = updatedAt,
            id = id,
        )
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
        createdAt: String,
        updatedAt: String,
    ) {
        equipmentResultQueries.insertResult(
            id = id,
            inspection_id = inspectionId,
            route_point_id = routePointId,
            equipment_id = equipmentId,
            status = status,
            started_at = null,
            completed_at = null,
            created_at = createdAt,
            updated_at = updatedAt,
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
        updatedAt: String,
    ) {
        equipmentResultQueries.updateStatus(
            status = status,
            started_at = startedAt,
            completed_at = completedAt,
            updated_at = updatedAt,
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
        selectedOption: String?,
        comment: String?,
        createdAt: String,
        updatedAt: String,
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

    override fun observeInspectionByAssignmentId(assignmentId: String): Flow<LocalInspection?> =
        inspectionQueries.selectByAssignmentId(assignmentId)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toLocal() }

    override fun observeEquipmentResultsByInspectionId(
        inspectionId: String,
    ): Flow<List<LocalEquipmentResult>> =
        equipmentResultQueries.selectByInspectionId(inspectionId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toLocal() } }

    override fun observeEquipmentResultByRoutePoint(
        routePointId: String,
        inspectionId: String,
    ): Flow<LocalEquipmentResult?> =
        equipmentResultQueries.selectByRoutePointAndInspection(routePointId, inspectionId)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toLocal() }

    override fun observeEquipmentResultById(id: String): Flow<LocalEquipmentResult?> =
        equipmentResultQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toLocal() }

    override fun observeChecklistItemResultsByEquipmentResult(
        equipmentResultId: String,
    ): Flow<List<LocalChecklistItemResult>> =
        checklistItemResultQueries.selectByEquipmentResultId(equipmentResultId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toLocal() } }

    private fun Inspections.toLocal() = LocalInspection(
        id = id,
        assignmentId = assignment_id,
        routeId = route_id,
        status = status,
        startedAt = started_at,
        completedAt = completed_at,
        createdAt = created_at,
        updatedAt = updated_at,
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
        createdAt = created_at,
        updatedAt = updated_at,
        syncStatus = sync_status,
    )

    private fun Checklist_item_results.toLocal() = LocalChecklistItemResult(
        id = id,
        equipmentResultId = inspection_equipment_result_id,
        checklistItemId = checklist_item_id,
        valueBoolean = value_boolean,
        valueNumber = value_number,
        valueText = value_text,
        selectedOption = selected_option,
        comment = comment,
        createdAt = created_at,
        updatedAt = updated_at,
        syncStatus = sync_status,
    )
}
