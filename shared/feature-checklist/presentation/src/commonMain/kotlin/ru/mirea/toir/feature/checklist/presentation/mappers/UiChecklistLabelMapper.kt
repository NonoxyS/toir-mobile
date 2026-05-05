package ru.mirea.toir.feature.checklist.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Label
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistLabel

interface UiChecklistLabelMapper : Mapper<Label, UiChecklistLabel>

internal class UiChecklistLabelMapperImpl : UiChecklistLabelMapper {

    override fun map(item: Label): UiChecklistLabel = when (item) {
        is Label.NavigateToPhotoCapture ->
            UiChecklistLabel.NavigateToPhotoCapture(item.checklistItemResultId)

        Label.ChecklistCompleted -> UiChecklistLabel.ChecklistCompleted
    }
}
