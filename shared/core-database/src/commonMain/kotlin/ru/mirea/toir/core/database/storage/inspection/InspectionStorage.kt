package ru.mirea.toir.core.database.storage.inspection

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection

interface InspectionStorage {

    @Suppress("LongParameterList")
    fun insertInspection(
        id: String,
        assignmentId: String?,
        routeId: String,
        status: LocalInspectionStatus,
        startedAt: String?,
        createdAt: String,
        updatedAt: String,
    )

    fun selectInspectionByAssignmentId(assignmentId: String): LocalInspection?

    fun selectInspectionById(id: String): LocalInspection?

    fun updateInspectionStatus(
        id: String,
        status: LocalInspectionStatus,
        completedAt: String?,
        updatedAt: String,
    )

    fun selectPendingInspections(): List<LocalInspection>

    fun updateInspectionSyncStatus(id: String, syncStatus: LocalSyncStatus)

    @Suppress("LongParameterList")
    fun insertEquipmentResult(
        id: String,
        inspectionId: String,
        routePointId: String,
        equipmentId: String,
        status: LocalEquipmentResultStatus,
        createdAt: String,
        updatedAt: String,
    )

    fun selectEquipmentResultsByInspectionId(inspectionId: String): List<LocalEquipmentResult>

    fun selectEquipmentResultById(id: String): LocalEquipmentResult?

    fun selectEquipmentResultByRoutePoint(
        routePointId: String,
        inspectionId: String,
    ): LocalEquipmentResult?

    @Suppress("LongParameterList")
    fun updateEquipmentResultStatus(
        id: String,
        status: LocalEquipmentResultStatus,
        startedAt: String?,
        completedAt: String?,
        updatedAt: String,
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
        selectedOption: String?,
        comment: String?,
        createdAt: String,
        updatedAt: String,
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

    fun observeInspectionByAssignmentId(assignmentId: String): Flow<LocalInspection?>

    fun observeEquipmentResultsByInspectionId(inspectionId: String): Flow<List<LocalEquipmentResult>>

    fun observeEquipmentResultByRoutePoint(
        routePointId: String,
        inspectionId: String,
    ): Flow<LocalEquipmentResult?>

    fun observeEquipmentResultById(id: String): Flow<LocalEquipmentResult?>

    fun observeChecklistItemResultsByEquipmentResult(
        equipmentResultId: String,
    ): Flow<List<LocalChecklistItemResult>>
}
