package ru.mirea.toir.core.database.storage.photo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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

    override fun selectPending(): List<LocalPhoto> =
        queries
            .selectPending()
            .executeAsList()
            .map { it.toLocal() }

    override fun updateSyncStatus(
        id: String,
        syncStatus: LocalSyncStatus,
        storageKey: String?
    ) {
        queries.updateSyncStatus(
            sync_status = syncStatus,
            storage_key = storageKey,
            id = id
        )
    }

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
    )
}
