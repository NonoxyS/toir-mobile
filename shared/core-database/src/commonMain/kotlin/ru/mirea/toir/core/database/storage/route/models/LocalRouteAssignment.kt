package ru.mirea.toir.core.database.storage.route.models

import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus

data class LocalRouteAssignment(
    val id: String,
    val routeId: String,
    val userId: String,
    val status: LocalRouteAssignmentStatus,
    val assignedAt: String,
    val shiftCode: String?,
    val updatedAt: String,
)
