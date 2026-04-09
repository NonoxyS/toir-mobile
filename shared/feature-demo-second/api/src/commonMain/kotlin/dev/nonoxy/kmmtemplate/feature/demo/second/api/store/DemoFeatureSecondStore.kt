package dev.nonoxy.kmmtemplate.feature.demo.second.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.Label
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.State

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
