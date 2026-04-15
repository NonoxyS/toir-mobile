package ru.mirea.toir.feature.route.points.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

interface RoutePointsStore : Store<RoutePointsStore.Intent, RoutePointsStore.State, RoutePointsStore.Label> {

    data class State(
        val inspectionId: String = "",
        val routeName: String = "",
        val points: List<DomainRoutePoint> = emptyList(),
        val isLoading: Boolean = true,
        val canFinish: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class Init(val inspectionId: String) : Intent
        data class OnPointClick(val routePointId: String) : Intent
        data object OnFinishInspection : Intent
    }

    sealed interface Label {
        data class NavigateToEquipmentCard(val inspectionId: String, val routePointId: String) : Label
        data object InspectionFinished : Label
    }
}
