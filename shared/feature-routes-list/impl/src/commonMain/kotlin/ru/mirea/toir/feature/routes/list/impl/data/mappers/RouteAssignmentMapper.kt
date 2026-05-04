package ru.mirea.toir.feature.routes.list.impl.data.mappers

import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.storage.route.models.LocalRoute
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus

internal class RouteAssignmentMapper {
    fun map(
        assignment: LocalRouteAssignment,
        route: LocalRoute?,
        totalPoints: Int,
        completedPoints: Int,
        inspectionId: String?,
        hasPendingSync: Boolean,
    ): DomainRouteAssignment = DomainRouteAssignment(
        assignmentId = assignment.id,
        routeId = assignment.routeId,
        routeName = route?.name.orEmpty(),
        status = assignment.status.toDomain(),
        assignedAt = assignment.assignedAt,
        totalPoints = totalPoints,
        completedPoints = completedPoints,
        inspectionId = inspectionId,
        hasPendingSync = hasPendingSync,
    )

    private fun LocalRouteAssignmentStatus.toDomain(): RouteAssignmentStatus = when (this) {
        LocalRouteAssignmentStatus.ASSIGNED -> RouteAssignmentStatus.ASSIGNED
        LocalRouteAssignmentStatus.IN_PROGRESS -> RouteAssignmentStatus.IN_PROGRESS
        LocalRouteAssignmentStatus.COMPLETED -> RouteAssignmentStatus.COMPLETED
        LocalRouteAssignmentStatus.PARTIALLY_COMPLETED -> RouteAssignmentStatus.PARTIALLY_COMPLETED
        LocalRouteAssignmentStatus.CANCELLED -> RouteAssignmentStatus.CANCELLED
    }
}
