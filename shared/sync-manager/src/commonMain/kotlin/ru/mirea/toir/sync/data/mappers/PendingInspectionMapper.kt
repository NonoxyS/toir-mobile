package ru.mirea.toir.sync.data.mappers

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalPendingInspection
import ru.mirea.toir.sync.domain.DomainPendingInspection
import ru.mirea.toir.sync.domain.PendingInspectionStatus

@OptIn(ExperimentalTime::class)
internal fun LocalPendingInspection.toDomain(): DomainPendingInspection =
    DomainPendingInspection(
        inspectionId = id,
        routeId = routeId,
        assignmentId = assignmentId,
        completedAt = completedAt?.let(Instant::parse),
        status = status.toPendingStatus(),
        attemptCount = attemptCount.toInt(),
        rejectionReason = rejectionReason?.toDomain(),
    )

private fun LocalInspectionStatus.toPendingStatus(): PendingInspectionStatus = when (this) {
    LocalInspectionStatus.COMPLETED -> PendingInspectionStatus.COMPLETED
    LocalInspectionStatus.PARTIALLY_COMPLETED -> PendingInspectionStatus.PARTIALLY_COMPLETED
    LocalInspectionStatus.CANCELLED -> PendingInspectionStatus.CANCELLED
    LocalInspectionStatus.PLANNED, LocalInspectionStatus.IN_PROGRESS ->
        error("Non-final status $this should not appear in pending inspections query")
}
