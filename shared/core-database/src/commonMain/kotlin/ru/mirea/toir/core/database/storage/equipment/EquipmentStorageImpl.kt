package ru.mirea.toir.core.database.storage.equipment

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.Equipment
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.storage.equipment.models.LocalEquipment

internal class EquipmentStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : EquipmentStorage {

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

    override fun observeEquipmentById(id: String): Flow<LocalEquipment?> =
        queries.selectById(id).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toLocal() }

    private fun Equipment.toLocal() = LocalEquipment(
        id = id,
        code = code,
        name = name,
        type = type,
        locationId = location_id,
        qrCode = qr_code,
    )
}
