package ru.mirea.toir.core.database.storage.action_log

import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalSyncStatus

internal class ActionLogStorageImpl(db: ToirDatabase) : ActionLogStorage {

    private val queries = db.actionLogQueries

    override fun insert(
        id: String,
        inspectionId: String,
        actionType: String,
        metadata: String?,
        createdAt: String,
    ) {
        queries.insertActionLog(
            id = id,
            inspection_id = inspectionId,
            action_type = actionType,
            metadata = metadata,
            created_at = createdAt,
            sync_status = LocalSyncStatus.PENDING.name,
        )
    }

    override fun selectPending(): List<LocalActionLog> =
        queries.selectPending().executeAsList().map { it.toLocal() }

    override fun updateSyncStatus(id: String, syncStatus: LocalSyncStatus) {
        queries.updateSyncStatus(sync_status = syncStatus.name, id = id)
    }

    private fun Action_logs.toLocal() = LocalActionLog(
        id = id,
        inspectionId = inspection_id,
        actionType = action_type,
        metadata = metadata,
        createdAt = created_at,
        syncStatus = LocalSyncStatus.fromString(sync_status),
    )
}
