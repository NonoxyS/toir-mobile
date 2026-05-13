package ru.mirea.toir.core.database.storage.sync_meta

import kotlinx.coroutines.flow.Flow

interface SyncMetaStorage {

    fun upsert(key: String, value: String)

    fun selectByKey(key: String): String?

    fun observeByKey(key: String): Flow<String?>

    companion object {
        const val KEY_LAST_SYNC_TIME = "last_sync_time"
        const val KEY_LAST_SYNC_AT_SUCCESS = "last_sync_at_success"
        const val KEY_LAST_SYNC_ERROR_REASON = "last_sync_error_reason"
        const val KEY_LAST_SYNC_ERROR_AT = "last_sync_error_at"
    }
}
