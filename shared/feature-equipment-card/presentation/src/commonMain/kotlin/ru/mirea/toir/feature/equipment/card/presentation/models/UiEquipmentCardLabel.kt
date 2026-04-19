package ru.mirea.toir.feature.equipment.card.presentation.models

sealed interface UiEquipmentCardLabel {
    data class NavigateToChecklist(val equipmentResultId: String) : UiEquipmentCardLabel
}
