package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalChecklistItemResult(
    val id: String,
    val equipmentResultId: String,
    val checklistItemId: String,
    val valueBoolean: Long?,
    val valueNumber: Double?,
    val valueText: String?,
    val selectedOption: String?,
    val comment: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: LocalSyncStatus,
    val syncAttemptCount: Long = 0L,
    val syncNextAttemptAt: String? = null,
    val syncLastError: String? = null,
)
