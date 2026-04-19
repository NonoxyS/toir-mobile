package ru.mirea.toir.core.database.storage.checklist

import ru.mirea.toir.core.database.Checklist_items
import ru.mirea.toir.core.database.Checklists
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklist
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklistItem

internal class ChecklistStorageImpl(db: ToirDatabase) : ChecklistStorage {

    private val checklistQueries = db.checklistQueries
    private val itemQueries = db.checklistItemQueries

    override fun upsertChecklist(
        id: String,
        name: String,
        equipmentId: String?
    ) {
        checklistQueries.upsertChecklist(
            id = id,
            name = name,
            equipment_id = equipmentId
        )
    }

    override fun selectChecklistById(id: String): LocalChecklist? =
        checklistQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun upsertItem(
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
            id = id,
            checklist_id = checklistId,
            title = title,
            description = description,
            answer_type = answerType,
            is_required = isRequired,
            requires_photo = requiresPhoto,
            select_options = selectOptions,
            order_index = orderIndex,
        )
    }

    override fun selectItemsByChecklistId(checklistId: String): List<LocalChecklistItem> =
        itemQueries
            .selectByChecklistId(checklistId)
            .executeAsList()
            .map { it.toLocal() }

    override fun selectItemById(id: String): LocalChecklistItem? =
        itemQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun deleteAll() {
        checklistQueries.deleteAll()
        itemQueries.deleteAll()
    }

    private fun Checklists.toLocal() = LocalChecklist(
        id = id,
        name = name,
        equipmentId = equipment_id,
    )

    private fun Checklist_items.toLocal() = LocalChecklistItem(
        id = id,
        checklistId = checklist_id,
        title = title,
        description = description,
        answerType = answer_type,
        isRequired = is_required,
        requiresPhoto = requires_photo,
        selectOptions = select_options,
        orderIndex = order_index,
    )
}
