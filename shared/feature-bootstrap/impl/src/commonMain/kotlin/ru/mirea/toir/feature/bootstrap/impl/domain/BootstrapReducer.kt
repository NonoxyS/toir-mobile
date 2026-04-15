package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.State
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory.Message

internal class BootstrapReducer : Reducer<State, Message> {
    override fun State.reduce(msg: Message): State = when (msg) {
        Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        Message.ClearLoading -> copy(isLoading = false)
    }
}
