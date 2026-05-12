package ru.mirea.toir.sync.domain

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.sync.domain.repository.SyncRepository

@OptIn(ExperimentalTime::class)
class SyncManager internal constructor(
    private val syncRepository: SyncRepository,
    private val actionLogger: ActionLogger,
    coroutineDispatchers: CoroutineDispatchers,
) {
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())
    private val mutex = Mutex()

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    val pendingCount: Flow<Long> = syncRepository.observePendingCount()

    fun syncNow(trigger: SyncTrigger): Job = scope.launch {
        runOnce(trigger)
    }

    suspend fun runOnce(trigger: SyncTrigger): Result<Unit> {
        if (trigger == SyncTrigger.Manual) {
            return mutex.withLock { executeSyncCycle(trigger) }
        }
        if (!mutex.tryLock()) {
            Napier.d("SyncManager: sync already running, skipping (trigger=$trigger)")
            return Result.success(Unit)
        }
        return try {
            executeSyncCycle(trigger)
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun executeSyncCycle(trigger: SyncTrigger): Result<Unit> {
        Napier.d("SyncManager: starting sync (trigger=$trigger)")
        actionLogger.log(actionType = ActionLogType.SYNC_STARTED)
        _status.value = SyncStatus.Running

        var pushedAccepted = 0L
        var uploadedPhotos = 0L
        var firstFailure: Throwable? = null

        syncRepository.uploadPendingPhotos()
            .onSuccess { count ->
                uploadedPhotos = count
                Napier.d("SyncManager: uploaded $count photos")
            }
            .onFailure { throwable ->
                firstFailure = throwable
                Napier.e("SyncManager: photo upload failed", throwable = throwable)
            }

        if (firstFailure == null) {
            syncRepository.pushPendingData()
                .onSuccess { result ->
                    pushedAccepted = result.acceptedCount.toLong()
                    Napier.d(
                        "SyncManager: push accepted=${result.acceptedCount} " +
                            "rejected=${result.rejectedCount}",
                    )
                }
                .onFailure { throwable ->
                    firstFailure = throwable
                    Napier.e("SyncManager: push failed", throwable = throwable)
                }
        }

        if (firstFailure == null) {
            syncRepository.fetchAndApplyDeltaChanges()
                .onSuccess { Napier.d("SyncManager: delta sync done") }
                .onFailure { throwable ->
                    firstFailure = throwable
                    Napier.e("SyncManager: delta sync failed", throwable = throwable)
                }
        }

        val finishedAt = Clock.System.now()
        val failure = firstFailure
        return if (failure == null) {
            syncRepository.recordSuccessfulRun(finishedAt)
            _status.value = SyncStatus.Success(
                finishedAt = finishedAt,
                pushedCount = pushedAccepted,
                uploadedPhotoCount = uploadedPhotos,
            )
            actionLogger.log(actionType = ActionLogType.SYNC_COMPLETED)
            Result.success(Unit)
        } else {
            val reason = failure.toSyncFailureReason()
            syncRepository.recordFailedRun(finishedAt, reason)
            _status.value = SyncStatus.Failed(finishedAt = finishedAt, reason = reason)
            actionLogger.log(actionType = ActionLogType.SYNC_FAILED)
            Result.failure(failure)
        }
    }
}
