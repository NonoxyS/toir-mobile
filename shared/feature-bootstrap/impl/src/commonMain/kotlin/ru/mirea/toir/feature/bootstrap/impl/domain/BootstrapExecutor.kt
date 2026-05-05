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
        when (bootstrapRepository.loadAndSaveBootstrap()) {
            BootstrapResult.Success -> {
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToRoutesList)
            }
            BootstrapResult.Unauthorized -> {
                authRepository.logout()
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToLogin)
            }
            is BootstrapResult.Failure -> {
                dispatch(Message.SetError)
            }
        }
    }
}
