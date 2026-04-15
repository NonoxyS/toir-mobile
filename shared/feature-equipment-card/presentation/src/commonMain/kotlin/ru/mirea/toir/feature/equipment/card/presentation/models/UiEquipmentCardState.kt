package ru.mirea.toir.feature.equipment.card.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiEquipmentCardState(
    val code: String = "",
    val name: String = "",
    val type: String = "",
    val locationName: String = "",
    val statusLabel: String = "",
    val equipmentResultId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
