package ru.mirea.toir.feature.equipment.card.presentation.mappers

import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState

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
            statusLabel = card?.inspectionStatus?.toStatusLabel().orEmpty(),
            equipmentResultId = card?.equipmentResultId,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
        )
    }

    private fun String.toStatusLabel(): String = when (this) {
        "NOT_STARTED" -> "Не проверено"
        "IN_PROGRESS" -> "Проверка в процессе"
        "COMPLETED" -> "Проверено"
        "SKIPPED" -> "Пропущено"
        else -> this
    }
}
