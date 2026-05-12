package ru.mirea.toir.sync.domain

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class DomainPendingInspection(
    val inspectionId: String,
    val routeId: String,
    val assignmentId: String?,
    val completedAt: Instant?,
    val status: PendingInspectionStatus,
    val attemptCount: Int,
    val rejectionReason: InspectionRejectionReason?,
)
