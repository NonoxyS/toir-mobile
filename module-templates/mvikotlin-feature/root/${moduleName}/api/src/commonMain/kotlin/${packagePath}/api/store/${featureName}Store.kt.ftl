package ${packageName}.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ${packageName}.api.store.${featureName}Store.Intent
import ${packageName}.api.store.${featureName}Store.Label
import ${packageName}.api.store.${featureName}Store.State

interface ${featureName}Store : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
    )

    sealed interface Intent {
        data object LoadData : Intent
    }

    sealed interface Label
}
