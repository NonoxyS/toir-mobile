package ru.mirea.toir.feature.routes.list.api.models

enum class RouteAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    PARTIALLY_COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromString(value: String): RouteAssignmentStatus =
            entries.firstOrNull { it.name == value } ?: ASSIGNED
    }
}
