package ru.mirea.toir.core.database.storage.action_log

import ru.mirea.toir.core.database.models.LocalSyncStatus

interface ActionLogStorage {

    fun insert(
        id: String,
        inspectionId: String,
        actionType: String,
        metadata: String?,
        createdAt: String,
    )

    fun selectPending(): List<LocalActionLog>

    fun updateSyncStatus(id: String, syncStatus: LocalSyncStatus)
}

data class LocalActionLog(
    val id: String,
    val inspectionId: String,
    val actionType: String,
    val metadata: String?,
    val createdAt: String,
    val syncStatus: LocalSyncStatus,
)
