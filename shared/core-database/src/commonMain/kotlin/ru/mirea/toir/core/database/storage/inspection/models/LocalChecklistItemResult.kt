package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalSyncStatus

data class LocalChecklistItemResult(
    val id: String,
    val equipmentResultId: String,
    val checklistItemId: String,
    val valueBoolean: Long?,
    val valueNumber: Double?,
    val valueText: String?,
    val valueSelect: String?,
    val isConfirmed: Long,
    val answeredAt: String?,
    val syncStatus: LocalSyncStatus,
)
