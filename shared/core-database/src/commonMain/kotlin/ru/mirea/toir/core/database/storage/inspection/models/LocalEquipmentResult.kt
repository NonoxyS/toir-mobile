package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalEquipmentResult(
    val id: String,
    val inspectionId: String,
    val routePointId: String,
    val equipmentId: String,
    val status: LocalEquipmentResultStatus,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: LocalSyncStatus,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncRejectionReason: LocalRejectionReason? = null,
)
