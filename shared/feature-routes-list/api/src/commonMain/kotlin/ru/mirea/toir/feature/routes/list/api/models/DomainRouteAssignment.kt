package ru.mirea.toir.feature.routes.list.api.models

data class DomainRouteAssignment(
    val assignmentId: String,
    val routeId: String,
    val routeCode: String,
    val routeName: String,
    val status: RouteAssignmentStatus,
    val assignedAt: String,
    val totalPoints: Int,
    val completedPoints: Int,
    val inspectionId: String?,
    val hasPendingSync: Boolean,
)
