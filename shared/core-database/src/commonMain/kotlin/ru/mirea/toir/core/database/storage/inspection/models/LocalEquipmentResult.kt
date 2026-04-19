package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalEquipmentResult(
    val id: String,
    val inspectionId: String,
    val routePointId: String,
    val equipmentId: String,
    val status: LocalEquipmentResultStatus,
    val startedAt: String?,
    val completedAt: String?,
    val syncStatus: LocalSyncStatus,
)
