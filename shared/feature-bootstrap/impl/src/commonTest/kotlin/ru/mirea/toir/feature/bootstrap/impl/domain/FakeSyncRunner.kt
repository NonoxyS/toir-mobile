package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import ru.mirea.toir.sync.domain.SyncTrigger
import ru.mirea.toir.sync.domain.SyncRunner

internal class FakeSyncRunner : SyncRunner {
    var callCount: Int = 0
        private set
    var lastTrigger: SyncTrigger? = null
        private set

    override fun syncNow(trigger: SyncTrigger): Job {
        callCount++
        lastTrigger = trigger
        return CompletableDeferred(Unit)
    }
}
