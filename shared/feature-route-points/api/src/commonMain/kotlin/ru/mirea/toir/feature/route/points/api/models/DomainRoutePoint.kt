package ru.mirea.toir.feature.route.points.api.models

import ru.mirea.toir.core.domain.models.EquipmentResultStatus

data class DomainRoutePoint(
    val routePointId: String,
    val equipmentId: String,
    val equipmentCode: String,
    val equipmentName: String,
    val locationName: String,
    val equipmentResultId: String?,
    val status: EquipmentResultStatus,
    val hasIssues: Boolean,
)
