package ru.mirea.toir.sync.domain.models

data class SyncResult(
    val acceptedCount: Int,
    val rejectedCount: Int,
)
