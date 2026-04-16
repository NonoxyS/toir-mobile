package ru.mirea.toir.feature.equipment.card.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Label
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel

interface UiEquipmentCardLabelMapper : Mapper<Label, UiEquipmentCardLabel>

internal class UiEquipmentCardLabelMapperImpl : UiEquipmentCardLabelMapper {
    override fun map(item: Label): UiEquipmentCardLabel = when (item) {
        is Label.NavigateToChecklist -> UiEquipmentCardLabel.NavigateToChecklist(item.equipmentResultId)
    }
}
