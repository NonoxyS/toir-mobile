package ru.mirea.toir.feature.auth.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiAuthState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
