package ru.mirea.toir.core.database.storage.sync_meta

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.ToirDatabase

internal class SyncMetaStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : SyncMetaStorage {

    private val queries = db.syncMetaQueries

    override fun upsert(key: String, value: String) {
        queries.upsert(key = key, value_ = value)
    }

    override fun selectByKey(key: String): String? =
        queries.selectByKey(key).executeAsOneOrNull()

    override fun observeByKey(key: String): Flow<String?> =
        queries.selectByKeyAsFlow(key)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
}
