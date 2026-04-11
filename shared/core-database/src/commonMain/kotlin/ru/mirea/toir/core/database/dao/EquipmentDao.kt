package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Equipment
import ru.mirea.toir.core.database.ToirDatabase

class EquipmentDao(db: ToirDatabase) {
    private val queries = db.equipmentQueries

    fun upsert(id: String, code: String, name: String, type: String, locationId: String) {
        queries.upsertEquipment(id = id, code = code, name = name, type = type, location_id = locationId)
    }

    fun selectAll(): List<Equipment> = queries.selectAll().executeAsList()

    fun selectById(id: String): Equipment? = queries.selectById(id).executeAsOneOrNull()

    fun deleteAll() = queries.deleteAll()
}
