package ru.mirea.toir.feature.checklist.impl.domain.repository

import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem

internal interface ChecklistRepository {
    suspend fun getChecklistItems(equipmentResultId: String): Result<List<DomainChecklistItem>>

    suspend fun saveBooleanAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Boolean,
    ): Result<Unit>

    suspend fun saveNumberAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Double,
    ): Result<Unit>

    suspend fun saveTextAnswer(
        equipmentResultId: String,
        itemId: String,
        value: String,
    ): Result<Unit>

    suspend fun saveSelectAnswer(
        equipmentResultId: String,
        itemId: String,
        value: String,
    ): Result<Unit>

    suspend fun saveConfirm(equipmentResultId: String, itemId: String): Result<Unit>

    suspend fun finishChecklist(equipmentResultId: String): Result<Unit>
}
