package ru.mirea.toir.core.database.storage.action_log

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalSyncStatus

internal class ActionLogStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : ActionLogStorage {

    private val queries = db.actionLogQueries

    override fun insert(
        id: String,
        actionType: String,
        entityType: String?,
        entityId: String?,
        payloadJson: String?,
        actionTime: String,
    ) {
        queries.insertActionLog(
            id = id,
            action_type = actionType,
            entity_type = entityType,
            entity_id = entityId,
            payload_json = payloadJson,
            action_time = actionTime,
            sync_status = LocalSyncStatus.PENDING,
        )
    }

    override fun selectAll(): List<LocalActionLog> =
        queries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectPending(now: String): List<LocalActionLog> =
        queries.selectPendingReady(now).executeAsList().map { it.toLocal() }

    override fun markSynced(id: String) {
        queries.markSynced(id = id)
    }

    override fun markRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        lastError: String?,
    ) {
        queries.markRetryScheduled(
            attemptCount = attemptCount,
            nextAt = nextAttemptAt,
            reason = lastError,
            id = id,
        )
    }

    override fun observePendingCount(): Flow<Long> =
        queries.selectPendingCount()
            .asFlow()
            .mapToOne(dispatchers.io)

    private fun Action_logs.toLocal() = LocalActionLog(
        id = id,
        actionType = action_type,
        entityType = entity_type,
        entityId = entity_id,
        payloadJson = payload_json,
        actionTime = action_time,
        syncStatus = sync_status,
        syncAttemptCount = sync_attempt_count,
        syncNextAttemptAt = sync_next_attempt_at,
        syncLastError = sync_last_error,
    )
}
