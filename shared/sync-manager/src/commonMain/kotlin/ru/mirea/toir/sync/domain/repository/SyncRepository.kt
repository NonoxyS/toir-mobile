package ru.mirea.toir.sync.domain.repository

import ru.mirea.toir.sync.domain.models.SyncResult

internal interface SyncRepository {
    suspend fun pushPendingData(): Result<SyncResult>
    suspend fun uploadPendingPhotos(): Result<Int>
    suspend fun fetchDeltaChanges(): Result<Unit>
}
