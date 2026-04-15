package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Intent
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory.Message
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapExecutor(
    private val bootstrapRepository: BootstrapRepository,
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
        bootstrapRepository.loadAndSaveBootstrap().fold(
            onSuccess = {
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToRoutesList)
            },
            onFailure = { throwable ->
                dispatch(Message.SetError(throwable.message ?: "Ошибка загрузки данных"))
            },
        )
        dispatch(Message.ClearLoading)
    }
}
