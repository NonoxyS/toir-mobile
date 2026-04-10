package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.ToirDatabase

internal class SyncMetaDao(db: ToirDatabase) {
    private val queries = db.syncMetaQueries

    fun upsert(key: String, value: String) {
        queries.upsert(key = key, value_ = value)
    }

    fun selectByKey(key: String): String? = queries.selectByKey(key).executeAsOneOrNull()

    companion object {
        const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
