package ru.mirea.toir.core.database.storage.checklist

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

data class LocalChecklist(
    val id: String,
    val name: String,
    val equipmentId: String?,
)

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
