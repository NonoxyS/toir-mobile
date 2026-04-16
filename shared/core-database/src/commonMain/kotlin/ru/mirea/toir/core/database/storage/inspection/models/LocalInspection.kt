package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalInspection(
    val id: String,
    val assignmentId: String,
    val routeId: String,
    val status: LocalRouteStatus,
    val startedAt: String,
    val completedAt: String?,
    val syncStatus: LocalSyncStatus,
)
