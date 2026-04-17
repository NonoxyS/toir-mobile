package ru.mirea.toir.core.database.storage.equipment

import ru.mirea.toir.core.database.Equipment
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.storage.equipment.models.LocalEquipment

internal class EquipmentStorageImpl(db: ToirDatabase) : EquipmentStorage {

    private val queries = db.equipmentQueries

    override fun upsert(
        id: String,
        code: String,
        name: String,
        type: String,
        locationId: String,
    ) {
        queries.upsertEquipment(
            id = id,
            code = code,
            name = name,
            type = type,
            location_id = locationId,
        )
    }

    override fun selectAll(): List<LocalEquipment> =
        queries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectById(id: String): LocalEquipment? =
        queries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun deleteAll() {
        queries.deleteAll()
    }

    private fun Equipment.toLocal() = LocalEquipment(
        id = id,
        code = code,
        name = name,
        type = type,
        locationId = location_id,
    )
}
