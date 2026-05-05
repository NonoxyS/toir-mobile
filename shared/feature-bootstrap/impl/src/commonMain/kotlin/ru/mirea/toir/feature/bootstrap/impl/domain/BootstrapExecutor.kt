package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Intent
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory.Message
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapExecutor(
    private val bootstrapRepository: BootstrapRepository,
    private val authRepository: AuthRepository,
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
            dispatch(Message.ClearLoading)
            publish(Label.NavigateToLogin)
            return
        }
        bootstrapRepository.loadAndSaveBootstrap().fold(
            onSuccess = {
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToRoutesList)
            },
            onFailure = {
                dispatch(Message.SetError)
            },
        )
    }
}
