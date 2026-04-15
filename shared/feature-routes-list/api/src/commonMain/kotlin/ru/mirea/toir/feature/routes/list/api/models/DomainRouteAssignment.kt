package ru.mirea.toir.feature.routes.list.api.models

data class DomainRouteAssignment(
    val assignmentId: String,
    val routeId: String,
    val routeName: String,
    val status: RouteAssignmentStatus,
    val assignedAt: String,
    val dueDate: String?,
    val totalPoints: Int,
    val completedPoints: Int,
    val inspectionId: String?,
    val hasPendingSync: Boolean,
)
