package ru.mirea.toir.sync.domain

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Running : SyncStatus
    data class Success(
        val finishedAt: Instant,
        val pushedCount: Int,
        val uploadedPhotoCount: Int,
    ) : SyncStatus
    data class Failed(
        val finishedAt: Instant,
        val reason: SyncFailureReason,
    ) : SyncStatus
}

enum class SyncFailureReason { NETWORK, AUTH, SERVER, UNKNOWN }
