package ru.mirea.toir.feature.bootstrap.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapExecutor(
    private val bootstrapRepository: BootstrapRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<BootstrapStore.Intent, Unit, BootstrapStore.State, BootstrapStoreFactory.Message, BootstrapStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        loadBootstrap()
    }

    override suspend fun suspendExecuteIntent(intent: BootstrapStore.Intent) {
        when (intent) {
            BootstrapStore.Intent.Retry -> loadBootstrap()
        }
    }

    private suspend fun loadBootstrap() {
        dispatch(BootstrapStoreFactory.Message.SetLoading)
        bootstrapRepository.loadAndSaveBootstrap().fold(
            onSuccess = {
                dispatch(BootstrapStoreFactory.Message.ClearLoading)
                publish(BootstrapStore.Label.NavigateToRoutesList)
            },
            onFailure = { throwable ->
                dispatch(BootstrapStoreFactory.Message.SetError(throwable.message ?: "Ошибка загрузки данных"))
            },
        )
    }
}
