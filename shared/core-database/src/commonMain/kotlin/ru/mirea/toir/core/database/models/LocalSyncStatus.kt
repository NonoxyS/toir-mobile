package ru.mirea.toir.core.database.models

enum class LocalSyncStatus {
    PENDING,
    SYNCED;

    companion object Companion {
        fun fromString(value: String): LocalSyncStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}
