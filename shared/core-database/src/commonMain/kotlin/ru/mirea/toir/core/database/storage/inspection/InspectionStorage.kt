package ru.mirea.toir.core.database.storage.inspection

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection
import ru.mirea.toir.core.database.storage.inspection.models.LocalPendingInspection

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

    fun selectPendingInspections(now: String): List<LocalInspection>

    fun markInspectionSynced(id: String)

    fun markInspectionRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
    )

    fun markInspectionRejected(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        reason: LocalRejectionReason,
    )

    fun observeHasPending(): Flow<Boolean>

    fun observePendingInspections(): Flow<List<LocalPendingInspection>>

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

    fun selectPendingEquipmentResults(now: String): List<LocalEquipmentResult>

    fun markEquipmentResultSynced(id: String)

    fun markEquipmentResultRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
    )

    fun markEquipmentResultRejected(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        reason: LocalRejectionReason,
    )

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

    fun selectPendingChecklistItemResults(now: String): List<LocalChecklistItemResult>

    fun markChecklistItemResultSynced(id: String)

    fun markChecklistItemResultRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
    )

    fun markChecklistItemResultRejected(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        reason: LocalRejectionReason,
    )

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

    /**
     * Apply a server copy of an inspection following the merge rule (Waypoint 11 §1.3):
     * insert when absent or update only if the local row is already `synced`;
     * pending/retry/rejected rows are preserved untouched.
     */
    @Suppress("LongParameterList")
    fun applyServerInspection(
        id: String,
        assignmentId: String?,
        routeId: String,
        status: LocalInspectionStatus,
        startedAt: String?,
        completedAt: String?,
        createdAt: String,
        updatedAt: String,
    )

    /** Same merge semantics as [applyServerInspection]. */
    @Suppress("LongParameterList")
    fun applyServerEquipmentResult(
        id: String,
        inspectionId: String,
        equipmentId: String,
        routePointId: String,
        status: LocalEquipmentResultStatus,
        startedAt: String?,
        completedAt: String?,
        createdAt: String,
        updatedAt: String,
    )

    /** Same merge semantics as [applyServerInspection]. */
    @Suppress("LongParameterList")
    fun applyServerChecklistItemResult(
        id: String,
        inspectionEquipmentResultId: String,
        checklistItemId: String,
        valueText: String?,
        valueNumber: Double?,
        valueBoolean: Long?,
        selectedOption: String?,
        comment: String?,
        createdAt: String,
        updatedAt: String,
    )
}
