package ru.mirea.toir.feature.auth.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.auth.api.store.AuthStore.State
import ru.mirea.toir.feature.auth.impl.domain.AuthStoreFactory.Message

internal class AuthReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetLogin -> copy(login = msg.value)
        is Message.SetPassword -> copy(password = msg.value)
        Message.SetLoading -> copy(isLoading = true, isError = false)
        Message.SetError -> copy(isLoading = false, isError = true)
        Message.ClearLoading -> copy(isLoading = false)
        Message.TogglePasswordVisibility -> copy(passwordVisible = !passwordVisible)
    }
}
