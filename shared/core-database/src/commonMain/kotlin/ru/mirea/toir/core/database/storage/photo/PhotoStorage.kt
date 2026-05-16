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

    /**
     * Metadata-only insert; `file_uri` stays null until the file is downloaded.
     * `storage_key` is omitted: on conflict a real local `storage_key` from a prior upload
     * is preserved. Same merge predicate as [applyServerInspection].
     */
    @Suppress("LongParameterList")
    fun insertRestoredPhoto(
        id: String,
        checklistItemResultId: String,
        takenAt: String,
        fileName: String?,
        mimeType: String?,
        sizeBytes: Long?,
        checksum: String?,
    )

    /** Restored photos whose file has not been downloaded yet. */
    fun selectMissingFiles(): List<LocalPhoto>

    /** Called after a restored photo's file is finally written to local storage. */
    fun setFileUri(id: String, fileUri: String)
}

data class LocalPhoto(
    val id: String,
    val checklistItemResultId: String,
    val fileUri: String?,
    val takenAt: String,
    val syncStatus: LocalSyncStatus,
    val storageKey: String?,
    val fileName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val checksum: String? = null,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncLastError: String? = null,
)
