package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore

internal class BootstrapReducer : Reducer<BootstrapStore.State, BootstrapStoreFactory.Message> {
    override fun BootstrapStore.State.reduce(msg: BootstrapStoreFactory.Message): BootstrapStore.State = when (msg) {
        BootstrapStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is BootstrapStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        BootstrapStoreFactory.Message.ClearLoading -> copy(isLoading = false)
    }
}
