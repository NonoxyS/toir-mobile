package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Intent
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory.Message
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult
import ru.mirea.toir.sync.domain.SyncTrigger
import ru.mirea.toir.sync.domain.SyncRunner

internal class BootstrapExecutor(
    private val bootstrapRepository: BootstrapRepository,
    private val authRepository: AuthRepository,
    private val syncRunner: SyncRunner,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Unit, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        loadBootstrap()
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.Retry -> loadBootstrap()
        }
    }

    private suspend fun loadBootstrap() {
        dispatch(Message.SetLoading)
        val tokens = authRepository.getBearerTokens().getOrNull()
        if (tokens == null) {
            publish(Label.NavigateToLogin)
            return
        }
        when (bootstrapRepository.loadAndSaveBootstrap()) {
            BootstrapResult.Success -> {
                syncRunner.syncNow(SyncTrigger.Bootstrap)
                publish(Label.NavigateToRoutesList)
            }

            BootstrapResult.Unauthorized -> {
                authRepository.logout()
                publish(Label.NavigateToLogin)
            }

            is BootstrapResult.Failure -> dispatch(Message.SetError)
        }
    }
}
