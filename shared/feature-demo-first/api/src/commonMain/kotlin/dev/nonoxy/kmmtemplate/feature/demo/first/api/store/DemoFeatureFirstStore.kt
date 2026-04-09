package dev.nonoxy.kmmtemplate.feature.demo.first.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.Label
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.State

interface DemoFeatureFirstStore : Store<Intent, State, Label> {

    data class State(
        val numberValue: String = "",
        val isLoading: Boolean = false,
        val isError: Boolean = false,
    )

    sealed interface Intent {
        data class OnNumberValueChange(val newValue: String) : Intent
        data object NextButtonClick : Intent
    }

    sealed interface Label {
        data object NavigateToSecondScreen : Label
    }
}
