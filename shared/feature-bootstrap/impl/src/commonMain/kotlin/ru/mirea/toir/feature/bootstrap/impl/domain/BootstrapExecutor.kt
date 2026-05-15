package ru.mirea.toir.feature.bootstrap.impl.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.common.extensions.coRunCatching
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
    /**
     * После успешного восстановления данных в bootstrap нужно запустить фоновый цикл,
     * чтобы Phase 5 (`SyncRepository.downloadMissingPhotos`) докачала файлы
     * восстановленных фото (`file_uri` = NULL после `PhotoStorage.insertRestoredPhoto`).
     * Передаётся лямбдой, а не `SyncManager`-зависимостью, чтобы избежать тестовых
     * приседаний с `internal constructor` SyncManager-а и оставить executor тонким.
     * DI собирает её как `{ syncManager.syncNow(SyncTrigger.Bootstrap) }`.
     */
    private val triggerBackgroundSync: () -> Unit,
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
                // Bootstrap мог восстановить photo-метаданные с `file_uri = NULL`.
                // Триггерим фоновый sync — Phase 5 (`downloadMissingPhotos`)
                // подхватит и докачает файлы. Лямбда возвращает Job сразу,
                // не блокирует переход на список маршрутов.
                triggerBackgroundSync()
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToRoutesList)
            }
            BootstrapResult.Unauthorized -> {
                coRunCatching(
                    tryBlock = { authRepository.logout() },
                    catchBlock = { cause ->
                        Napier.w(message = "logout failed during 401 handling", throwable = cause)
                    },
                )
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToLogin)
            }
            is BootstrapResult.Failure -> {
                dispatch(Message.SetError)
            }
        }
    }
}
