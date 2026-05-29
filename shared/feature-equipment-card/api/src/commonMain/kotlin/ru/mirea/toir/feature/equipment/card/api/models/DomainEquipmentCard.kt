package ru.mirea.toir.feature.equipment.card.api.models

import ru.mirea.toir.core.domain.models.EquipmentResultStatus

data class DomainEquipmentCard(
    val equipmentId: String,
    val code: String,
    val name: String,
    val type: String,
    val locationName: String,
    val equipmentResultId: String,
    val inspectionStatus: EquipmentResultStatus,
)
