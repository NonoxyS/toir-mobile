package ru.mirea.toir.core.database.storage.route.models

data class LocalRoutePoint(
    val id: String,
    val routeId: String,
    val equipmentId: String,
    val checklistId: String,
    val orderIndex: Long,
)
