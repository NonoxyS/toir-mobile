package ru.mirea.toir.core.database.models

enum class LocalRouteStatus(
    override val localValue: String
) : LocalEnum {

    ASSIGNED("assigned"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed")
}
