package ru.mirea.toir.feature.routes.list.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory.Message

internal class RoutesListReducer : Reducer<State, Message> {
    override fun State.reduce(msg: Message): State = when (msg) {
        Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        is Message.SetAssignments -> copy(isLoading = false, assignments = msg.list, errorMessage = null)
    }
}
