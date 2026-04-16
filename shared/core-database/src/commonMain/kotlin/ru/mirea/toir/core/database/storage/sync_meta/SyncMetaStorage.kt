package ru.mirea.toir.core.database.storage.sync_meta

interface SyncMetaStorage {

    fun upsert(key: String, value: String)

    fun selectByKey(key: String): String?

    companion object {
        const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
