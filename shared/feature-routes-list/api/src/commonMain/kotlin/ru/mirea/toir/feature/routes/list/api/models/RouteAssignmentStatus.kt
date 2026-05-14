package ru.mirea.toir.feature.routes.list.api.models

enum class RouteAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    PARTIALLY_COMPLETED,
    CANCELLED,

    // Сервер сообщает, что инспекция уже идёт (например, начата на другом устройстве),
    // но локально соответствующей Inspection нет. Не фолбэк к ASSIGNED: создавать новую
    // запись запрещено — пользователю нужно потянуть актуальные данные синхронизацией.
    SYNC_REQUIRED,
    ;

    companion object {
        fun fromString(value: String): RouteAssignmentStatus =
            entries.firstOrNull { it.name == value } ?: ASSIGNED
    }
}
