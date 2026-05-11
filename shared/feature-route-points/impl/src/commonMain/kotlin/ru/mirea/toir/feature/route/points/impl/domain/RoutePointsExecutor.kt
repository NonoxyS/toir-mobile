package ru.mirea.toir.feature.route.points.impl.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Intent
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Label
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.State
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory.Action
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory.Message
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

internal class RoutePointsExecutor(
    private val repository: RoutePointsRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Action, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    private var subscriptionJob: Job? = null

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.Load -> subscribeToRoutePoints(state().inspectionId)
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnPointClick -> publish(
                Label.NavigateToEquipmentCard(state().inspectionId, intent.routePointId)
            )
            Intent.OnFinishInspection -> finishInspection()
        }
    }

    private fun subscribeToRoutePoints(inspectionId: String) {
        subscriptionJob?.cancel()
        subscriptionJob = repository.observeRoutePoints(inspectionId)
            .onStart { dispatch(Message.SetLoading) }
            .onEach { (routeName, points) ->
                dispatch(Message.SetData(routeName = routeName, points = points))
            }
            .catch { throwable ->
                Napier.e(message = "observeRoutePoints failed", throwable = throwable)
                dispatch(Message.SetError)
            }
            .launchIn(scope)
    }

    private suspend fun finishInspection() {
        val inspectionId = state().inspectionId
        repository.finishInspection(inspectionId).fold(
            onSuccess = { publish(Label.InspectionFinished) },
            onFailure = { throwable ->
                Napier.e(message = "finishInspection failed", throwable = throwable)
                dispatch(Message.SetError)
            },
        )
    }
}
