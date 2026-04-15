package ru.mirea.toir.feature.route.points.presentation.models

sealed interface UiRoutePointsLabel {
    data class NavigateToEquipmentCard(val inspectionId: String, val routePointId: String) : UiRoutePointsLabel
    data object InspectionFinished : UiRoutePointsLabel
}
