package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason

data class LocalPendingInspection(
    val id: String,
    val assignmentId: String?,
    val routeId: String,
    val status: LocalInspectionStatus,
    val completedAt: String?,
    val attemptCount: Long,
    val rejectionReason: LocalRejectionReason?,
)
