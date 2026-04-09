package ru.mirea.toir.feature.demo.first.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.Label
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.State

internal class DemoFeatureFirstStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
) {

    fun create(): DemoFeatureFirstStore =
        object :
            DemoFeatureFirstStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = DemoFeatureFirstStore::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = {
                    DemoFeatureFirstExecutor(
                        mainDispatcher = mainDispatcher
                    )
                },
                reducer = DemoFeatureFirstReducer()
            ) {}

    sealed interface Message {
        data class SetNumberValue(val value: String) : Message
        data class SetLoading(val isLoading: Boolean) : Message
        data class SetError(val isError: Boolean) : Message
    }
}
