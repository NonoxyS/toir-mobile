package ru.mirea.toir.core.database.storage.checklist

import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklist
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklistItem

interface ChecklistStorage {

    fun upsertChecklist(id: String, name: String, equipmentId: String?)

    fun selectChecklistById(id: String): LocalChecklist?

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
        orderIndex: Long,
    )

    fun selectItemsByChecklistId(checklistId: String): List<LocalChecklistItem>

    fun selectItemById(id: String): LocalChecklistItem?

    fun deleteAll()
}
