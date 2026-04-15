package ru.mirea.toir.feature.route.points.impl.domain

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
) {
    fun create(): RoutePointsStore =
        object :
            RoutePointsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoutePointsStore::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = { RoutePointsExecutor(repository, mainDispatcher) },
                reducer = RoutePointsReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data class SetData(
            val inspectionId: String,
            val routeName: String,
            val points: List<DomainRoutePoint>,
        ) : Message
    }
}
