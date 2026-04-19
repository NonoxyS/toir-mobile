package ru.mirea.toir.core.database.storage.checklist.models

data class LocalChecklistItem(
    val id: String,
    val checklistId: String,
    val title: String,
    val description: String?,
    val answerType: String,
    val isRequired: Long,
    val requiresPhoto: Long,
    val selectOptions: String?,
    val orderIndex: Long,
)
