package ru.mirea.toir.core.database.storage.photo

import ru.mirea.toir.core.database.models.LocalSyncStatus

interface PhotoStorage {

    fun insert(
        id: String,
        checklistItemResultId: String,
        fileUri: String,
        takenAt: String,
    )

    fun selectByChecklistItemResultId(checklistItemResultId: String): List<LocalPhoto>

    fun selectPending(): List<LocalPhoto>

    fun updateSyncStatus(id: String, syncStatus: LocalSyncStatus, storageKey: String?)
}

data class LocalPhoto(
    val id: String,
    val checklistItemResultId: String,
    val fileUri: String,
    val takenAt: String,
    val syncStatus: LocalSyncStatus,
    val storageKey: String?,
)
