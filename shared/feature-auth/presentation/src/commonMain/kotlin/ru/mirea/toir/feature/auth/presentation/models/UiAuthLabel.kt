package ru.mirea.toir.feature.auth.presentation.models

sealed interface UiAuthLabel {
    data object NavigateToMain : UiAuthLabel
}
