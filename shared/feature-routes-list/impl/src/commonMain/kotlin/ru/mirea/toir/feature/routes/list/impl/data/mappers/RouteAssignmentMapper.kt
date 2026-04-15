package ru.mirea.toir.feature.routes.list.impl.data.mappers

import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Routes
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus

internal class RouteAssignmentMapper {
    fun map(
        assignment: Route_assignments,
        route: Routes?,
        totalPoints: Int,
        completedPoints: Int,
        inspectionId: String?,
        hasPendingSync: Boolean,
    ): DomainRouteAssignment = DomainRouteAssignment(
        assignmentId = assignment.id,
        routeId = assignment.route_id,
        routeName = route?.name.orEmpty(),
        status = assignment.status.toDomain(),
        assignedAt = assignment.assigned_at,
        dueDate = assignment.due_date,
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
