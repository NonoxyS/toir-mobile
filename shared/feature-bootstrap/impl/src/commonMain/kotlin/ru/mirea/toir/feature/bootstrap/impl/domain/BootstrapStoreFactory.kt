package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Intent
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapStoreFactory(
    private val storeFactory: StoreFactory,
    private val bootstrapRepository: BootstrapRepository,
    private val authRepository: AuthRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(autoInit: Boolean = true): BootstrapStore =
        object :
            BootstrapStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = BootstrapStore::class.simpleName,
                autoInit = autoInit,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = {
                    BootstrapExecutor(bootstrapRepository, authRepository, mainDispatcher)
                },
                reducer = BootstrapReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data object SetError : Message
        data object ClearLoading : Message
    }
}
