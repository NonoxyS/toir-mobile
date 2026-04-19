package ru.mirea.toir.feature.route.points.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.route.points.api.models.EquipmentResultStatus
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory.Message

internal class RoutePointsReducer : Reducer<RoutePointsStore.State, Message> {
    override fun RoutePointsStore.State.reduce(msg: Message): RoutePointsStore.State = when (msg) {
        Message.SetLoading -> copy(isLoading = true, isError = false)
        Message.SetError -> copy(isLoading = false, isError = true)
        is Message.SetData -> copy(
            isLoading = false,
            inspectionId = msg.inspectionId,
            routeName = msg.routeName,
            points = msg.points,
            canFinish = msg.points.any { it.status == EquipmentResultStatus.COMPLETED },
        )
    }
}
