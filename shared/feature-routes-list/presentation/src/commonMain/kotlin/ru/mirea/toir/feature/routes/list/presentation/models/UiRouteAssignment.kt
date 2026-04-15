package ru.mirea.toir.feature.routes.list.presentation.models

data class UiRouteAssignment(
    val assignmentId: String,
    val routeName: String,
    val routeNumber: String,
    val status: UiRouteStatus,
    val completedPoints: Int,
    val totalPoints: Int,
    val progress: Float,
    val assignedAt: String,
    val inspectionId: String?,
    val hasPendingSync: Boolean,
)
