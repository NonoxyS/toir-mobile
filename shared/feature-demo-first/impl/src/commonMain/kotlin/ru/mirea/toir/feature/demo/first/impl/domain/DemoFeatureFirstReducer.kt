package ru.mirea.toir.feature.demo.first.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.State
import ru.mirea.toir.feature.demo.first.impl.domain.DemoFeatureFirstStoreFactory.Message

internal class DemoFeatureFirstReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetNumberValue -> copy(numberValue = msg.value)
        is Message.SetLoading -> copy(isLoading = msg.isLoading)
        is Message.SetError -> copy(isError = msg.isError)
    }
}
