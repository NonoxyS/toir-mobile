package ru.mirea.toir.feature.auth.api.store

import com.arkivanov.mvikotlin.core.store.Store

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, AuthStore.Label> {

    data class State(
        val login: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class OnLoginChange(val value: String) : Intent
        data class OnPasswordChange(val value: String) : Intent
        data object OnLoginClick : Intent
    }

    sealed interface Label {
        data object NavigateToMain : Label
    }
}
