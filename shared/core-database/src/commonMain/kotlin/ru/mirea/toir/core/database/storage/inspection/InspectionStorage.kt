package ru.mirea.toir.core.database.storage.inspection

import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection

interface InspectionStorage {

    fun insertInspection(
        id: String,
        assignmentId: String,
        routeId: String,
        status: LocalRouteStatus,
        startedAt: String,
    )

    fun selectInspectionByAssignmentId(assignmentId: String): LocalInspection?

    fun selectInspectionById(id: String): LocalInspection?

    fun updateInspectionStatus(id: String, status: LocalRouteStatus, completedAt: String?)

    fun selectPendingInspections(): List<LocalInspection>

    fun updateInspectionSyncStatus(id: String, syncStatus: LocalSyncStatus)

    fun insertEquipmentResult(
        id: String,
        inspectionId: String,
        routePointId: String,
        equipmentId: String,
        status: LocalEquipmentResultStatus,
    )

    fun selectEquipmentResultsByInspectionId(inspectionId: String): List<LocalEquipmentResult>

    fun selectEquipmentResultById(id: String): LocalEquipmentResult?

    fun selectEquipmentResultByRoutePoint(
        routePointId: String,
        inspectionId: String,
    ): LocalEquipmentResult?

    fun updateEquipmentResultStatus(
        id: String,
        status: LocalEquipmentResultStatus,
        startedAt: String?,
        completedAt: String?,
    )

    fun selectPendingEquipmentResults(): List<LocalEquipmentResult>

    fun updateEquipmentResultSyncStatus(id: String, syncStatus: LocalSyncStatus)

    @Suppress("LongParameterList")
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
    )

    fun selectChecklistItemResultsByEquipmentResult(
        equipmentResultId: String,
    ): List<LocalChecklistItemResult>

    fun selectChecklistItemResult(
        checklistItemId: String,
        equipmentResultId: String,
    ): LocalChecklistItemResult?

    fun selectPendingChecklistItemResults(): List<LocalChecklistItemResult>

    fun updateChecklistItemResultSyncStatus(id: String, syncStatus: LocalSyncStatus)
}
