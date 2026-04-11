package ru.mirea.toir.feature.auth.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.auth.api.store.AuthStore.Intent
import ru.mirea.toir.feature.auth.api.store.AuthStore.Label
import ru.mirea.toir.feature.auth.api.store.AuthStore.State
import ru.mirea.toir.feature.auth.impl.domain.AuthStoreFactory.Message
import ru.mirea.toir.core.auth.domain.repository.AuthRepository

internal class AuthExecutor(
    private val authRepository: AuthRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Nothing, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnLoginChange -> dispatch(Message.SetLogin(intent.value))
            is Intent.OnPasswordChange -> dispatch(Message.SetPassword(intent.value))
            Intent.OnLoginClick -> handleLogin()
            Intent.TogglePasswordVisibility -> dispatch(Message.TogglePasswordVisibility)
        }
    }

    private suspend fun handleLogin() {
        val currentState = state()
        if (currentState.isLoading) return
        dispatch(Message.SetLoading)

        authRepository.login(
            login = currentState.login,
            password = currentState.password,
        ).fold(
            onSuccess = {
                dispatch(Message.ClearLoading)
                publish(Label.NavigateToMain)
            },
            onFailure = { throwable ->
                dispatch(Message.SetError(throwable.message ?: "Ошибка авторизации"))
            },
        )
    }
}
