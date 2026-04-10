package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Checklist_items
import ru.mirea.toir.core.database.Checklists
import ru.mirea.toir.core.database.ToirDatabase

internal class ChecklistDao(db: ToirDatabase) {
    private val checklistQueries = db.checklistQueries
    private val itemQueries = db.checklistItemQueries

    fun upsertChecklist(id: String, name: String, equipmentId: String?) {
        checklistQueries.upsertChecklist(id = id, name = name, equipment_id = equipmentId)
    }

    fun selectById(id: String): Checklists? = checklistQueries.selectById(id).executeAsOneOrNull()

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
    ) {
        itemQueries.upsertChecklistItem(
            id = id, checklist_id = checklistId, title = title, description = description,
            answer_type = answerType, is_required = isRequired, requires_photo = requiresPhoto,
            select_options = selectOptions, order_index = orderIndex,
        )
    }

    fun selectItemsByChecklistId(checklistId: String): List<Checklist_items> =
        itemQueries.selectByChecklistId(checklistId).executeAsList()

    fun selectItemById(id: String): Checklist_items? = itemQueries.selectById(id).executeAsOneOrNull()

    fun deleteAll() {
        checklistQueries.deleteAll()
        itemQueries.deleteAll()
    }
}
