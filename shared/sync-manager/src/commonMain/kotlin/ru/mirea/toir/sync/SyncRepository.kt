package ru.mirea.toir.sync

internal interface SyncRepository {
    suspend fun pushPendingData(): Result<SyncResult>
    suspend fun uploadPendingPhotos(): Result<Int>
    suspend fun fetchDeltaChanges(): Result<Unit>
}

data class SyncResult(
    val acceptedCount: Int,
    val rejectedCount: Int,
)
