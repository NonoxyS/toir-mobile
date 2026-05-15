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
     * Insert metadata for a server-restored photo. `file_uri` stays `null` until the file
     * is downloaded; the row is marked `synced` immediately. `storage_key` is not provided:
     * the download endpoint takes `photoId`, and on conflict any real `storage_key` already
     * stored locally for a previously-uploaded row is preserved. On conflict with an existing
     * row, the merge rule (Waypoint 11 §1.3) applies — pending/retry/rejected rows are
     * preserved untouched.
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
