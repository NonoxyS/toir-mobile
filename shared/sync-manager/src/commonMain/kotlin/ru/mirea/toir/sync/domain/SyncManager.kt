package ru.mirea.toir.sync.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.sync.domain.repository.SyncRepository

class SyncManager internal constructor(
    private val syncRepository: SyncRepository,
    coroutineDispatchers: CoroutineDispatchers,
) {
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())

    fun syncNow() {
        scope.launch {
            Napier.d("SyncManager: starting sync")
            syncRepository.pushPendingData().onSuccess { result ->
                Napier.d(
                    "SyncManager: push success — accepted=${result.acceptedCount}, rejected=${result.rejectedCount}"
                )
            }
            syncRepository.uploadPendingPhotos().onSuccess { count ->
                Napier.d("SyncManager: uploaded $count photos")
            }
            syncRepository.fetchDeltaChanges().onSuccess {
                Napier.d("SyncManager: delta sync done")
            }
        }
    }
}
