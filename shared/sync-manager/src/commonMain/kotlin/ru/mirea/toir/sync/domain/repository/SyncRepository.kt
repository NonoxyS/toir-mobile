package ru.mirea.toir.sync.domain.repository

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.sync.domain.DomainPendingInspection
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.models.SyncResult

@OptIn(ExperimentalTime::class)
internal interface SyncRepository {
    suspend fun pushPendingData(): Result<SyncResult>
    suspend fun uploadPendingPhotos(): Result<Long>

    /**
     * Downloads any photos whose metadata is restored locally but whose file is not yet on
     * disk (`file_uri IS NULL`). Returns the count of photos successfully downloaded.
     * Per-photo failures are logged and skipped — the row stays in `selectMissingFiles` and
     * is retried on the next sync cycle. Returns `Result.failure` only on a fatal error
     * that prevents any download attempt at all.
     */
    suspend fun downloadMissingPhotos(): Result<Long>
    suspend fun fetchAndApplyDeltaChanges(): Result<Unit>

    fun observeHasPending(): Flow<Boolean>
    fun observePendingInspections(): Flow<List<DomainPendingInspection>>

    suspend fun recordSuccessfulRun(finishedAt: Instant)
    suspend fun recordFailedRun(finishedAt: Instant, reason: SyncFailureReason)
}
