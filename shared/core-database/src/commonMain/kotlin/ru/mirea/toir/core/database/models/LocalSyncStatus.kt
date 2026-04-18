package ru.mirea.toir.core.database.models

enum class LocalSyncStatus(
    override val localValue: String
) : LocalEnum {

    PENDING("pending"),
    SYNCED("synced")
}
