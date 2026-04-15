package ru.mirea.toir.feature.route.points.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiRoutePoint(
    val routePointId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val statusLabel: String,
    val statusColor: String, // "default" | "success" | "warning" | "error"
    val hasIssues: Boolean,
    val equipmentResultId: String?,
)
