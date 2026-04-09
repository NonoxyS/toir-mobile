package ru.mirea.toir.feature.demo.second.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Label
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.State

interface DemoFeatureSecondStore : Store<Intent, State, Label> {

    data class State(
        val joke: String? = null,
        val isLoading: Boolean = true,
        val isError: Boolean = false,
    )

    sealed interface Intent {
        data object RefreshClick : Intent
    }

    sealed interface Label
}
