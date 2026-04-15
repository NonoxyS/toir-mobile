package ru.mirea.toir.core.database.models

enum class LocalRouteStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED;

    companion object Companion {
        fun fromString(value: String): LocalRouteStatus =
            entries.firstOrNull { it.name == value } ?: ASSIGNED
    }
}
