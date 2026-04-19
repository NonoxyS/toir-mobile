package ru.mirea.toir.core.database.storage.equipment.models

data class LocalEquipment(
    val id: String,
    val code: String,
    val name: String,
    val type: String,
    val locationId: String?,
)
