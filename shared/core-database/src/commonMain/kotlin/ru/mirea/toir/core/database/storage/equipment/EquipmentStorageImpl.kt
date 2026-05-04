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
        locationId: String?,
        qrCode: String?,
    ) {
        queries.upsertEquipment(
            id = id,
            code = code,
            name = name,
            type = type,
            location_id = locationId,
            qr_code = qrCode,
        )
    }

    override fun selectAll(): List<LocalEquipment> =
        queries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectById(id: String): LocalEquipment? =
        queries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun selectByQrCode(qrCode: String): LocalEquipment? =
        queries.selectByQrCode(qrCode).executeAsOneOrNull()?.toLocal()

    override fun deleteAll() {
        queries.deleteAll()
    }

    override fun deleteById(id: String) {
        queries.deleteById(id)
    }

    private fun Equipment.toLocal() = LocalEquipment(
        id = id,
        code = code,
        name = name,
        type = type,
        locationId = location_id,
        qrCode = qr_code,
    )
}
