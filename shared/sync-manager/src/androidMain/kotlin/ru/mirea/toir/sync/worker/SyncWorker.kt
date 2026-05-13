package ru.mirea.toir.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mirea.toir.sync.domain.SyncManager
import ru.mirea.toir.sync.domain.SyncTrigger

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val syncManager: SyncManager by inject()

    override suspend fun doWork(): Result =
        if (syncManager.runOnce(trigger = SyncTrigger.Periodic).isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
}
