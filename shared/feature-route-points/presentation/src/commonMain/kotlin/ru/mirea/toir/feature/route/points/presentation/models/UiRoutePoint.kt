package ru.mirea.toir.feature.route.points.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiRoutePoint(
    val routePointId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val status: UiEquipmentResultStatus,
    val hasIssues: Boolean,
    val equipmentResultId: String?,
)
