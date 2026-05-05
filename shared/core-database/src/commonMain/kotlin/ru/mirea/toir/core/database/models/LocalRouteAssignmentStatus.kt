package ru.mirea.toir.core.database.models

enum class LocalRouteAssignmentStatus(
    override val localValue: String
) : LocalEnum {

    ASSIGNED("assigned"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    PARTIALLY_COMPLETED("partially_completed"),
    CANCELLED("cancelled")
}
