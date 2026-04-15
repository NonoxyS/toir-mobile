package ru.mirea.toir.feature.equipment.card.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel

interface UiEquipmentCardLabelMapper : Mapper<EquipmentCardStore.Label, UiEquipmentCardLabel>

internal class UiEquipmentCardLabelMapperImpl : UiEquipmentCardLabelMapper {
    override fun map(item: EquipmentCardStore.Label): UiEquipmentCardLabel = when (item) {
        is EquipmentCardStore.Label.NavigateToChecklist ->
            UiEquipmentCardLabel.NavigateToChecklist(item.equipmentResultId)
    }
}
