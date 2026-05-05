package ru.mirea.toir.sync.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.sync.domain.repository.SyncRepository

class SyncManager internal constructor(
    private val syncRepository: SyncRepository,
    private val actionLogger: ActionLogger,
    coroutineDispatchers: CoroutineDispatchers,
) {
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())
    private val mutex = Mutex()

    fun syncNow(): Job = scope.launch {
        if (!mutex.tryLock()) {
            Napier.d("SyncManager: sync already running, skipping")
            return@launch
        }
        try {
            Napier.d("SyncManager: starting sync")
            actionLogger.log(actionType = ActionLogType.SYNC_STARTED)

            var allOk = true

            syncRepository.uploadPendingPhotos()
                .onSuccess { count -> Napier.d("SyncManager: uploaded $count photos") }
                .onFailure { throwable ->
                    allOk = false
                    Napier.e("SyncManager: photo upload failed; continuing", throwable = throwable)
                }

            syncRepository.pushPendingData()
                .onSuccess { result ->
                    Napier.d(
                        "SyncManager: push success — accepted=${result.acceptedCount}, rejected=${result.rejectedCount}"
                    )
                    if (result.rejectedCount > 0) allOk = false
                }
                .onFailure { throwable ->
                    allOk = false
                    Napier.e("SyncManager: push failed", throwable = throwable)
                }

            syncRepository.fetchAndApplyDeltaChanges()
                .onSuccess { Napier.d("SyncManager: delta sync done") }
                .onFailure { throwable ->
                    allOk = false
                    Napier.e("SyncManager: delta sync failed", throwable = throwable)
                }

            actionLogger.log(
                actionType = if (allOk) ActionLogType.SYNC_COMPLETED else ActionLogType.SYNC_FAILED,
            )
        } finally {
            mutex.unlock()
        }
    }

    suspend fun syncBlocking() = mutex.withLock {
        syncRepository.uploadPendingPhotos()
        syncRepository.pushPendingData()
        syncRepository.fetchAndApplyDeltaChanges()
    }
}
