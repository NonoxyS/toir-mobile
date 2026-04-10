package ru.mirea.toir.feature.auth.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.auth.api.store.AuthStore

internal class AuthReducer : Reducer<AuthStore.State, AuthStoreFactory.Message> {

    override fun AuthStore.State.reduce(msg: AuthStoreFactory.Message): AuthStore.State = when (msg) {
        is AuthStoreFactory.Message.SetLogin -> copy(login = msg.value)
        is AuthStoreFactory.Message.SetPassword -> copy(password = msg.value)
        AuthStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is AuthStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        AuthStoreFactory.Message.ClearLoading -> copy(isLoading = false)
    }
}
