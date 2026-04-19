package ru.mirea.toir.feature.equipment.card.presentation.models

data class UiEquipmentCardState(
    val code: String = "",
    val name: String = "",
    val type: String = "",
    val locationName: String = "",
    val status: UiEquipmentResultStatus = UiEquipmentResultStatus.NOT_STARTED,
    val equipmentResultId: String? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)
