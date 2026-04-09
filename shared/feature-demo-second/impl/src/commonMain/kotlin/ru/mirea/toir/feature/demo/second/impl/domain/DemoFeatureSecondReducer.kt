package ru.mirea.toir.feature.demo.second.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.State
import ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Message

internal class DemoFeatureSecondReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetJoke -> copy(joke = msg.joke)
        is Message.SetLoading -> copy(isLoading = msg.isLoading)
        is Message.SetError -> copy(isError = msg.isError)
    }
}
