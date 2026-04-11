package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Intent
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapStoreFactory(
    private val storeFactory: StoreFactory,
    private val bootstrapRepository: BootstrapRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): BootstrapStore =
        object :
            BootstrapStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = BootstrapStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { BootstrapExecutor(bootstrapRepository, mainDispatcher) },
                reducer = BootstrapReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data object ClearLoading : Message
    }
}
