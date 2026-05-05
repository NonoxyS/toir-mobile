package ru.mirea.toir.core.database.storage.location

import ru.mirea.toir.core.database.storage.location.models.LocalLocation

interface LocationStorage {

    fun upsert(
        id: String,
        code: String,
        name: String,
        description: String?,
        parentLocationId: String?,
    )

    fun selectAll(): List<LocalLocation>

    fun selectById(id: String): LocalLocation?

    fun deleteAll()

    fun deleteById(id: String)
}
