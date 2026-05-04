package ru.mirea.toir.core.database.storage.location.models

data class LocalLocation(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val parentLocationId: String?,
)
