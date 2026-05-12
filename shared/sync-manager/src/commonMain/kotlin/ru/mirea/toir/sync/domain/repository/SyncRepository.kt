package ru.mirea.toir.sync.domain.repository

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.models.SyncResult

@OptIn(ExperimentalTime::class)
internal interface SyncRepository {
    suspend fun pushPendingData(): Result<SyncResult>
    suspend fun uploadPendingPhotos(): Result<Long>
    suspend fun fetchAndApplyDeltaChanges(): Result<Unit>

    fun observePendingCount(): Flow<Long>

    suspend fun recordSuccessfulRun(finishedAt: Instant)
    suspend fun recordFailedRun(finishedAt: Instant, reason: SyncFailureReason)
}
