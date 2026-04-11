package ru.mirea.toir.feature.auth.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapper
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthState

class AuthViewModel internal constructor(
    private val store: AuthStore,
    private val stateMapper: UiAuthStateMapper,
    private val labelMapper: UiAuthLabelMapper,
) : BaseViewModel<UiAuthState, UiAuthLabel>(initialState = UiAuthState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onLoginChange(value: String) = store.accept(AuthStore.Intent.OnLoginChange(value))
    fun onPasswordChange(value: String) = store.accept(AuthStore.Intent.OnPasswordChange(value))
    fun onLoginClick() = store.accept(AuthStore.Intent.OnLoginClick)
    fun onTogglePasswordVisibility() = store.accept(AuthStore.Intent.TogglePasswordVisibility)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
