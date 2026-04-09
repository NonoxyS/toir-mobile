package ru.mirea.toir.feature.demo.first.presentation.models

sealed interface UiDemoFeatureFirstLabel {
    data object NavigateToSecondScreen : UiDemoFeatureFirstLabel
}
