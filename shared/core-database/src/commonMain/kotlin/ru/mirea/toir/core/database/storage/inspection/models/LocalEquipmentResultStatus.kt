package ru.mirea.toir.core.database.storage.inspection.models

enum class LocalEquipmentResultStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED;

    companion object {
        fun fromString(value: String): LocalEquipmentResultStatus =
            entries.firstOrNull { it.name == value } ?: NOT_STARTED
    }
}
