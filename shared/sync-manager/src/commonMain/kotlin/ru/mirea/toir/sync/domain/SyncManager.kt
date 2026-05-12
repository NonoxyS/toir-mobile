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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorage
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.sync.domain.repository.SyncRepository

@OptIn(ExperimentalTime::class)
class SyncManager internal constructor(
    private val syncRepository: SyncRepository,
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val actionLogStorage: ActionLogStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val actionLogger: ActionLogger,
    coroutineDispatchers: CoroutineDispatchers,
) {
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())
    private val mutex = Mutex()

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    val pendingCount: Flow<Long> = combine(
        inspectionStorage.observeInspectionPendingCount(),
        inspectionStorage.observeEquipmentResultPendingCount(),
        inspectionStorage.observeChecklistItemResultPendingCount(),
        photoStorage.observePhotoPendingCount(),
        actionLogStorage.observePendingCount(),
    ) { a, b, c, d, e -> a + b + c + d + e }

    fun syncNow(trigger: SyncTrigger): Job = scope.launch {
        runOnce(trigger)
    }

    suspend fun runOnce(trigger: SyncTrigger): Result<Unit> {
        if (!mutex.tryLock()) {
            Napier.d("SyncManager: sync already running, skipping (trigger=$trigger)")
            return Result.success(Unit)
        }
        return try {
            Napier.d("SyncManager: starting sync (trigger=$trigger)")
            actionLogger.log(actionType = ActionLogType.SYNC_STARTED)
            _status.value = SyncStatus.Running

            var pushedAccepted = 0
            var uploadedPhotos = 0
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
                        pushedAccepted = result.acceptedCount
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
            if (failure == null) {
                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_AT_SUCCESS,
                    value = finishedAt.toString(),
                )
                _status.value = SyncStatus.Success(
                    finishedAt = finishedAt,
                    pushedCount = pushedAccepted,
                    uploadedPhotoCount = uploadedPhotos,
                )
                actionLogger.log(actionType = ActionLogType.SYNC_COMPLETED)
                Result.success(Unit)
            } else {
                val reason = failure.toSyncFailureReason()
                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_ERROR_REASON,
                    value = reason.name,
                )
                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_ERROR_AT,
                    value = finishedAt.toString(),
                )
                _status.value = SyncStatus.Failed(finishedAt = finishedAt, reason = reason)
                actionLogger.log(actionType = ActionLogType.SYNC_FAILED)
                Result.failure(failure)
            }
        } finally {
            mutex.unlock()
        }
    }
}
