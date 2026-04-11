package ru.mirea.toir.feature.bootstrap.api.store

import com.arkivanov.mvikotlin.core.store.Store

interface BootstrapStore : Store<BootstrapStore.Intent, BootstrapStore.State, BootstrapStore.Label> {

    data class State(
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object Retry : Intent
    }

    sealed interface Label {
        data object NavigateToRoutesList : Label
        data object NavigateToLogin : Label
    }
}
