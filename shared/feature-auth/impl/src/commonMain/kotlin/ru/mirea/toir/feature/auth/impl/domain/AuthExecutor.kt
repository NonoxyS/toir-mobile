package ru.mirea.toir.feature.auth.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository

internal class AuthExecutor(
    private val authRepository: AuthRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<AuthStore.Intent, Nothing, AuthStore.State, AuthStoreFactory.Message, AuthStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: AuthStore.Intent) {
        when (intent) {
            is AuthStore.Intent.OnLoginChange -> dispatch(AuthStoreFactory.Message.SetLogin(intent.value))
            is AuthStore.Intent.OnPasswordChange -> dispatch(AuthStoreFactory.Message.SetPassword(intent.value))
            AuthStore.Intent.OnLoginClick -> handleLogin()
        }
    }

    private suspend fun handleLogin() {
        val currentState = state()
        if (currentState.isLoading) return
        dispatch(AuthStoreFactory.Message.SetLoading)

        authRepository.login(
            login = currentState.login,
            password = currentState.password,
        ).fold(
            onSuccess = {
                dispatch(AuthStoreFactory.Message.ClearLoading)
                publish(AuthStore.Label.NavigateToMain)
            },
            onFailure = { throwable ->
                dispatch(AuthStoreFactory.Message.SetError(throwable.message ?: "Ошибка авторизации"))
            },
        )
    }
}
