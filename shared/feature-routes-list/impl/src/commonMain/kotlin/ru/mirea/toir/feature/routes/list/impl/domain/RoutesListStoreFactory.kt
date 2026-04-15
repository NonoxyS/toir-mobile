package ru.mirea.toir.feature.routes.list.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Intent
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Label
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository

internal class RoutesListStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RoutesListRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): RoutesListStore =
        object :
            RoutesListStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoutesListStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { RoutesListExecutor(repository, mainDispatcher) },
                reducer = RoutesListReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data class SetAssignments(val list: List<DomainRouteAssignment>) : Message
    }
}
