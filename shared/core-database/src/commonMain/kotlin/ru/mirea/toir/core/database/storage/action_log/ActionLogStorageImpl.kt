package ru.mirea.toir.core.database.storage.action_log

import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalSyncStatus

internal class ActionLogStorageImpl(db: ToirDatabase) : ActionLogStorage {

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

    override fun selectPending(): List<LocalActionLog> =
        queries.selectPending().executeAsList().map { it.toLocal() }

    override fun updateSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        queries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    private fun Action_logs.toLocal() = LocalActionLog(
        id = id,
        actionType = action_type,
        entityType = entity_type,
        entityId = entity_id,
        payloadJson = payload_json,
        actionTime = action_time,
        syncStatus = sync_status,
    )
}
