package ru.mirea.toir.feature.equipment.card.presentation.mappers

import ru.mirea.toir.feature.equipment.card.api.models.EquipmentResultStatus
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus

interface UiEquipmentCardStateMapper {
    fun map(state: EquipmentCardStore.State): UiEquipmentCardState
}

internal class UiEquipmentCardStateMapperImpl : UiEquipmentCardStateMapper {
    override fun map(state: EquipmentCardStore.State): UiEquipmentCardState {
        val card = state.card
        return UiEquipmentCardState(
            code = card?.code.orEmpty(),
            name = card?.name.orEmpty(),
            type = card?.type.orEmpty(),
            locationName = card?.locationName.orEmpty(),
            status = card?.inspectionStatus?.toUi() ?: UiEquipmentResultStatus.NOT_STARTED,
            equipmentResultId = card?.equipmentResultId,
            isLoading = state.isLoading,
            isError = state.isError,
        )
    }

    private fun EquipmentResultStatus.toUi(): UiEquipmentResultStatus = when (this) {
        EquipmentResultStatus.NOT_STARTED -> UiEquipmentResultStatus.NOT_STARTED
        EquipmentResultStatus.IN_PROGRESS -> UiEquipmentResultStatus.IN_PROGRESS
        EquipmentResultStatus.COMPLETED -> UiEquipmentResultStatus.COMPLETED
        EquipmentResultStatus.SKIPPED -> UiEquipmentResultStatus.SKIPPED
    }
}
