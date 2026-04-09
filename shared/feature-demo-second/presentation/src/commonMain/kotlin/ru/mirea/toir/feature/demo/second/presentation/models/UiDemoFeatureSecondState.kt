package ru.mirea.toir.feature.demo.second.presentation.models

data class UiDemoFeatureSecondState(
    val joke: String? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)
