package ru.mirea.toir.core.database.storage.location

import ru.mirea.toir.core.database.Locations
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.storage.location.models.LocalLocation

internal class LocationStorageImpl(db: ToirDatabase) : LocationStorage {

    private val queries = db.locationQueries

    override fun upsert(
        id: String,
        code: String,
        name: String,
        description: String?,
        parentLocationId: String?,
    ) {
        queries.upsertLocation(
            id = id,
            code = code,
            name = name,
            description = description,
            parent_location_id = parentLocationId,
        )
    }

    override fun selectAll(): List<LocalLocation> =
        queries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectById(id: String): LocalLocation? =
        queries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun deleteAll() {
        queries.deleteAll()
    }

    override fun deleteById(id: String) {
        queries.deleteById(id)
    }

    private fun Locations.toLocal() = LocalLocation(
        id = id,
        code = code,
        name = name,
        description = description,
        parentLocationId = parent_location_id,
    )
}
