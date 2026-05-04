package ru.mirea.toir.feature.route.points.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Intent
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Label
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.State
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

internal class RoutePointsStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RoutePointsRepository,
    private val mainDispatcher: CoroutineDispatcher,
    private val inspectionId: String,
) {
    fun create(): RoutePointsStore =
        object :
            RoutePointsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoutePointsStore::class.simpleName,
                initialState = State(inspectionId = inspectionId),
                bootstrapper = SimpleBootstrapper(Action.Load),
                executorFactory = { RoutePointsExecutor(repository, mainDispatcher) },
                reducer = RoutePointsReducer(),
            ) {}

    internal sealed interface Action {
        data object Load : Action
    }

    internal sealed interface Message {
        data object SetLoading : Message
        data object SetError : Message
        data class SetData(
            val routeName: String,
            val points: List<DomainRoutePoint>,
        ) : Message
    }
}
