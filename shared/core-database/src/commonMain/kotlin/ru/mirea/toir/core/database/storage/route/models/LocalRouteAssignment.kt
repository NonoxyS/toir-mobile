package ru.mirea.toir.core.database.storage.route.models

import ru.mirea.toir.core.database.models.LocalRouteStatus

data class LocalRouteAssignment(
    val id: String,
    val routeId: String,
    val userId: String,
    val status: LocalRouteStatus,
    val assignedAt: String,
    val dueDate: String?,
)
