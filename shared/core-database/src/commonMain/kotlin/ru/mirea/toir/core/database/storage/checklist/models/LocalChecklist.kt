package ru.mirea.toir.core.database.storage.checklist.models

data class LocalChecklist(
    val id: String,
    val code: String,
    val name: String,
    val equipmentType: String,
    val description: String?,
)
