package ru.mirea.toir.core.database.storage.action_log

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

    fun selectPending(): List<LocalActionLog>

    fun updateSyncStatus(id: String, syncStatus: LocalSyncStatus)
}

data class LocalActionLog(
    val id: String,
    val actionType: String,
    val entityType: String?,
    val entityId: String?,
    val payloadJson: String?,
    val actionTime: String,
    val syncStatus: LocalSyncStatus,
)
