package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.ToirDatabase

internal class ActionLogDao(db: ToirDatabase) {
    private val queries = db.actionLogQueries

    fun insert(
        id: String,
        inspectionId: String,
        actionType: String,
        metadata: String?,
        createdAt: String
    ) {
        queries.insertActionLog(
            id = id,
            inspection_id = inspectionId,
            action_type = actionType,
            metadata = metadata,
            created_at = createdAt,
            sync_status = "PENDING",
        )
    }

    fun selectPending(): List<Action_logs> = queries.selectPending().executeAsList()

    fun updateSyncStatus(id: String, syncStatus: String) {
        queries.updateSyncStatus(sync_status = syncStatus, id = id)
    }
}
