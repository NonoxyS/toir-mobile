package ru.mirea.toir.feature.route.points.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Intent
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Label
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.State
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory.Message
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

internal class RoutePointsExecutor(
    private val repository: RoutePointsRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Nothing, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.Init -> loadPoints(intent.inspectionId)
            is Intent.OnPointClick -> publish(
                Label.NavigateToEquipmentCard(state().inspectionId, intent.routePointId)
            )
            Intent.OnFinishInspection -> finishInspection()
        }
    }

    private suspend fun loadPoints(inspectionId: String) {
        dispatch(Message.SetLoading)
        repository.getRoutePoints(inspectionId).fold(
            onSuccess = { (routeName, points) ->
                dispatch(
                    Message.SetData(
                    inspectionId = inspectionId,
                    routeName = routeName,
                    points = points,
                )
                )
            },
            onFailure = { throwable ->
                dispatch(Message.SetError)
            },
        )
    }

    private suspend fun finishInspection() {
        val inspectionId = state().inspectionId
        repository.finishInspection(inspectionId).fold(
            onSuccess = { publish(Label.InspectionFinished) },
            onFailure = { throwable ->
                dispatch(Message.SetError)
            },
        )
    }
}
