package ru.mirea.toir.feature.routes.list.impl.data.mappers

import ru.mirea.toir.core.database.storage.route.models.LocalRoute
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus

internal class RouteAssignmentMapper {
    fun map(
        assignment: LocalRouteAssignment,
        route: LocalRoute?,
        status: RouteAssignmentStatus,
        totalPoints: Int,
        completedPoints: Int,
        inspectionId: String?,
        hasPendingSync: Boolean,
    ): DomainRouteAssignment = DomainRouteAssignment(
        assignmentId = assignment.id,
        routeId = assignment.routeId,
        routeName = route?.name.orEmpty(),
        status = status,
        assignedAt = assignment.assignedAt,
        totalPoints = totalPoints,
        completedPoints = completedPoints,
        inspectionId = inspectionId,
        hasPendingSync = hasPendingSync,
    )
}
