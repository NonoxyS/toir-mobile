package ru.mirea.toir.feature.equipment.card.api.models

enum class EquipmentResultStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED;

    companion object {
        fun fromString(value: String): EquipmentResultStatus =
            entries.firstOrNull { it.name == value } ?: NOT_STARTED
    }
}
