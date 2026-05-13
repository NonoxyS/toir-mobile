package ru.mirea.toir.core.database.storage.photo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.Photos
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalSyncStatus

internal class PhotoStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : PhotoStorage {

    private val queries = db.photoQueries

    override fun insert(
        id: String,
        checklistItemResultId: String,
        fileUri: String,
        takenAt: String,
    ) {
        queries.insertPhoto(
            id = id,
            checklist_item_result_id = checklistItemResultId,
            file_uri = fileUri,
            taken_at = takenAt,
            sync_status = LocalSyncStatus.PENDING,
            storage_key = null,
        )
    }

    override fun selectByChecklistItemResultId(checklistItemResultId: String): List<LocalPhoto> =
        queries
            .selectByChecklistItemResultId(checklistItemResultId)
            .executeAsList()
            .map { it.toLocal() }

    override fun observePhotosByEquipmentResultId(
        equipmentResultId: String,
    ): Flow<List<LocalPhoto>> =
        queries.selectByEquipmentResultId(equipmentResultId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toLocal() } }

    override fun selectPendingPhotos(now: String): List<LocalPhoto> =
        queries
            .selectPendingReady(now)
            .executeAsList()
            .map { it.toLocal() }

    override fun markPhotoSynced(id: String, storageKey: String?) {
        queries.markSynced(storageKey = storageKey, id = id)
    }

    override fun markPhotoRetryScheduled(
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

    override fun observePhotoPendingCount(): Flow<Long> =
        queries.selectPendingCount()
            .asFlow()
            .mapToOne(dispatchers.io)

    override fun delete(id: String) {
        queries.deletePhoto(id)
    }

    private fun Photos.toLocal() = LocalPhoto(
        id = id,
        checklistItemResultId = checklist_item_result_id,
        fileUri = file_uri,
        takenAt = taken_at,
        syncStatus = sync_status,
        storageKey = storage_key,
        syncAttemptCount = sync_attempt_count,
        syncNextAttemptAt = sync_next_attempt_at,
        syncLastError = sync_last_error,
    )
}
