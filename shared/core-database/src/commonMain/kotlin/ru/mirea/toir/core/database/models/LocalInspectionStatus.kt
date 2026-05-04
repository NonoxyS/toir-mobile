package ru.mirea.toir.core.database.models

enum class LocalInspectionStatus(
    override val localValue: String
) : LocalEnum {

    PLANNED("planned"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    PARTIALLY_COMPLETED("partially_completed"),
    CANCELLED("cancelled")
}
