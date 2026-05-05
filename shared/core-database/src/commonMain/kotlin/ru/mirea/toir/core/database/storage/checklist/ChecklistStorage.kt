package ru.mirea.toir.core.database.storage.checklist

import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklist
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklistItem

interface ChecklistStorage {

    fun upsertChecklist(
        id: String,
        code: String,
        name: String,
        equipmentType: String,
        description: String?,
    )

    fun selectChecklistById(id: String): LocalChecklist?

    fun selectChecklistByEquipmentType(equipmentType: String): LocalChecklist?

    fun deleteChecklistById(id: String)

    @Suppress("LongParameterList")
    fun upsertItem(
        id: String,
        checklistId: String,
        title: String,
        description: String?,
        answerType: String,
        isRequired: Long,
        requiresPhoto: Long,
        selectOptions: String?,
        numericMin: Double?,
        numericMax: Double?,
        orderIndex: Long,
    )

    fun selectItemsByChecklistId(checklistId: String): List<LocalChecklistItem>

    fun selectItemById(id: String): LocalChecklistItem?

    fun deleteItemById(id: String)

    fun deleteAll()
}
