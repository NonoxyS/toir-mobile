package ru.mirea.toir.feature.routes.list.presentation.models

sealed interface UiRoutesListLabel {
    data class NavigateToRoutePoints(val inspectionId: String) : UiRoutesListLabel
}
