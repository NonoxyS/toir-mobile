package ru.mirea.toir.core.database.models

enum class LocalBatchStatus(
    override val localValue: String
) : LocalEnum {

    PENDING("pending"),
    SENT("sent"),
    FAILED("failed")
}
