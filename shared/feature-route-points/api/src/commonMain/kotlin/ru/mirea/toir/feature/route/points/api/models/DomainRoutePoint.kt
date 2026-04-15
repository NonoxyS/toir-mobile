package ru.mirea.toir.feature.route.points.api.models

data class DomainRoutePoint(
    val routePointId: String,
    val equipmentId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val equipmentResultId: String?,
    val status: String,
    val hasIssues: Boolean,
)
