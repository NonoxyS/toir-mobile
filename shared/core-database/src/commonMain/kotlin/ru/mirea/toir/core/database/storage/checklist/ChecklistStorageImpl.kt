package ru.mirea.toir.core.database.storage.checklist

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.Checklist_items
import ru.mirea.toir.core.database.Checklists
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklist
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklistItem

internal class ChecklistStorageImpl(
    db: ToirDatabase,
    private val dispatchers: CoroutineDispatchers,
) : ChecklistStorage {

    private val checklistQueries = db.checklistQueries
    private val itemQueries = db.checklistItemQueries

    override fun upsertChecklist(
        id: String,
        code: String,
        name: String,
        equipmentType: String,
        description: String?,
    ) {
        checklistQueries.upsertChecklist(
            id = id,
            code = code,
            name = name,
            equipment_type = equipmentType,
            description = description,
        )
    }

    override fun selectChecklistById(id: String): LocalChecklist? =
        checklistQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun selectChecklistByEquipmentType(equipmentType: String): LocalChecklist? =
        checklistQueries.selectByEquipmentType(equipmentType).executeAsOneOrNull()?.toLocal()

    override fun deleteChecklistById(id: String) {
        checklistQueries.deleteById(id)
    }

    override fun upsertItem(
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
            numeric_min = numericMin,
            numeric_max = numericMax,
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

    override fun deleteItemById(id: String) {
        itemQueries.deleteById(id)
    }

    override fun deleteAll() {
        checklistQueries.deleteAll()
        itemQueries.deleteAll()
    }

    override fun observeItemsByChecklistId(checklistId: String): Flow<List<LocalChecklistItem>> =
        itemQueries.selectByChecklistId(checklistId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toLocal() } }

    private fun Checklists.toLocal() = LocalChecklist(
        id = id,
        code = code,
        name = name,
        equipmentType = equipment_type,
        description = description,
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
        numericMin = numeric_min,
        numericMax = numeric_max,
        orderIndex = order_index,
    )
}
