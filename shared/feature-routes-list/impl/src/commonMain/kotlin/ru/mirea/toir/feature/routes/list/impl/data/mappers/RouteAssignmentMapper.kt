package ru.mirea.toir.feature.routes.list.impl.data.mappers

import ru.mirea.toir.core.database.models.LocalRouteStatus
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
        dueDate = assignment.dueDate,
        totalPoints = totalPoints,
        completedPoints = completedPoints,
        inspectionId = inspectionId,
        hasPendingSync = hasPendingSync,
    )

    private fun LocalRouteStatus.toDomain(): RouteAssignmentStatus = when (this) {
        LocalRouteStatus.ASSIGNED -> RouteAssignmentStatus.ASSIGNED
        LocalRouteStatus.IN_PROGRESS -> RouteAssignmentStatus.IN_PROGRESS
        LocalRouteStatus.COMPLETED -> RouteAssignmentStatus.COMPLETED
    }
}
