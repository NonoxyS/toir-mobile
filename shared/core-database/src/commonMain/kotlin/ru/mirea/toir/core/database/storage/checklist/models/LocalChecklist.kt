package ru.mirea.toir.core.database.storage.checklist.models

data class LocalChecklist(
    val id: String,
    val name: String,
    val equipmentId: String?,
)
