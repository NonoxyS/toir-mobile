package ru.mirea.toir.feature.routes.list.api.models

enum class RouteAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    ;

    companion object {
        fun fromString(value: String): RouteAssignmentStatus =
            entries.firstOrNull { it.name == value } ?: ASSIGNED
    }
}
