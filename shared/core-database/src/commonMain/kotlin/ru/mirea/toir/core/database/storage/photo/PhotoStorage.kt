package ru.mirea.toir.core.database.storage.photo

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.core.database.models.LocalSyncStatus

interface PhotoStorage {

    fun insert(
        id: String,
        checklistItemResultId: String,
        fileUri: String,
        takenAt: String,
    )

    fun selectByChecklistItemResultId(checklistItemResultId: String): List<LocalPhoto>

    fun observePhotosByEquipmentResultId(
        equipmentResultId: String,
    ): Flow<List<LocalPhoto>>

    fun selectPendingPhotos(now: String): List<LocalPhoto>

    fun markPhotoSynced(id: String, storageKey: String?)

    fun markPhotoRetryScheduled(
        id: String,
        attemptCount: Long,
        nextAttemptAt: String,
        lastError: String?,
    )

    fun observePhotoPendingCount(): Flow<Long>

    fun delete(id: String)
}

data class LocalPhoto(
    val id: String,
    val checklistItemResultId: String,
    val fileUri: String,
    val takenAt: String,
    val syncStatus: LocalSyncStatus,
    val storageKey: String?,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncLastError: String? = null,
)
