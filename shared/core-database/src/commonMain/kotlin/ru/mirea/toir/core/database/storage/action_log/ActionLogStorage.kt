package ru.mirea.toir.core.database.storage.action_log

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.core.database.models.LocalSyncStatus

interface ActionLogStorage {

    @Suppress("LongParameterList")
    fun insert(
        id: String,
        actionType: String,
        entityType: String?,
        entityId: String?,
        payloadJson: String?,
        actionTime: String,
    )

    fun selectAll(): List<LocalActionLog>

    fun selectPending(now: String): List<LocalActionLog>

    fun markSynced(id: String)

    fun markRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        lastError: String?,
    )

    fun observePendingCount(): Flow<Long>
}

data class LocalActionLog(
    val id: String,
    val actionType: String,
    val entityType: String?,
    val entityId: String?,
    val payloadJson: String?,
    val actionTime: String,
    val syncStatus: LocalSyncStatus,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncLastError: String? = null,
)
