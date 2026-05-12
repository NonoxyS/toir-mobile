package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalInspection(
    val id: String,
    val assignmentId: String?,
    val routeId: String,
    val status: LocalInspectionStatus,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: LocalSyncStatus,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncRejectionReason: LocalRejectionReason? = null,
)
