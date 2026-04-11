package ru.mirea.toir.feature.bootstrap.presentation.models

sealed interface UiBootstrapLabel {
    data object NavigateToRoutesList : UiBootstrapLabel
    data object NavigateToLogin : UiBootstrapLabel
}
