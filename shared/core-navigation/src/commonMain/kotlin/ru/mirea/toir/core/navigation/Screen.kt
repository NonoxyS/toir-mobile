package ru.mirea.toir.core.navigation

import kotlinx.serialization.Serializable

interface Screen

@Serializable
data object AuthRoute : Screen

@Serializable
data object BootstrapRoute : Screen

@Serializable
data object RoutesListRoute : Screen

@Serializable
data class RoutePointsRoute(val inspectionId: String) : Screen

@Serializable
data class EquipmentCardRoute(
    val inspectionId: String,
    val routePointId: String,
) : Screen

@Serializable
data class ChecklistRoute(val equipmentResultId: String) : Screen

@Serializable
data class PhotoCaptureRoute(val checklistItemResultId: String) : Screen

@Serializable
data object DemoFirstRoute : Screen

@Serializable
data object DemoSecondRoute : Screen
