package ru.mirea.toir.feature.bootstrap.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiBootstrapState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
