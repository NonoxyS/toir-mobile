package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Photos
import ru.mirea.toir.core.database.ToirDatabase

internal class PhotoDao(db: ToirDatabase) {
    private val queries = db.photoQueries

    fun insert(id: String, checklistItemResultId: String, fileUri: String, takenAt: String) {
        queries.insertPhoto(
            id = id,
            checklist_item_result_id = checklistItemResultId,
            file_uri = fileUri,
            taken_at = takenAt,
            sync_status = "PENDING",
            storage_key = null,
        )
    }

    fun selectByChecklistItemResultId(checklistItemResultId: String): List<Photos> =
        queries.selectByChecklistItemResultId(checklistItemResultId).executeAsList()

    fun selectPending(): List<Photos> = queries.selectPending().executeAsList()

    fun updateSyncStatus(id: String, syncStatus: String, storageKey: String?) {
        queries.updateSyncStatus(sync_status = syncStatus, storage_key = storageKey, id = id)
    }
}
