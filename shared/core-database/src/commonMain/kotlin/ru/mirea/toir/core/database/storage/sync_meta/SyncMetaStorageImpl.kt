package ru.mirea.toir.core.database.storage.sync_meta

import ru.mirea.toir.core.database.ToirDatabase

internal class SyncMetaStorageImpl(db: ToirDatabase) : SyncMetaStorage {

    private val queries = db.syncMetaQueries

    override fun upsert(key: String, value: String) {
        queries.upsert(key = key, value_ = value)
    }

    override fun selectByKey(key: String): String? =
        queries.selectByKey(key).executeAsOneOrNull()
}
