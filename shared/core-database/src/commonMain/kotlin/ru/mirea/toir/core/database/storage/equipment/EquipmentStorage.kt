package ru.mirea.toir.core.database.storage.equipment

import ru.mirea.toir.core.database.storage.equipment.models.LocalEquipment

interface EquipmentStorage {

    fun upsert(
        id: String,
        code: String,
        name: String,
        type: String,
        locationId: String?,
        qrCode: String?,
    )

    fun selectAll(): List<LocalEquipment>

    fun selectById(id: String): LocalEquipment?

    fun selectByQrCode(qrCode: String): LocalEquipment?

    fun deleteAll()

    fun deleteById(id: String)
}
