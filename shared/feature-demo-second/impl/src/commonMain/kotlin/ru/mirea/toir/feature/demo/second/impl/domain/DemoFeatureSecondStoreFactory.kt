package ru.mirea.toir.feature.demo.second.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Label
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.State
import ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository
import kotlinx.coroutines.CoroutineDispatcher

internal class DemoFeatureSecondStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val jokeRepository: ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository,
) {

    fun create(): DemoFeatureSecondStore =
        object :
            DemoFeatureSecondStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = DemoFeatureSecondStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadJoke),
                executorFactory = {
                    _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondExecutor(
                        mainDispatcher = mainDispatcher,
                        jokeRepository = jokeRepository,
                    )
                },
                reducer = _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondReducer(),
            ) {}

    sealed interface Message {
        data class SetJoke(val joke: String) : Message
        data class SetLoading(val isLoading: Boolean) : Message
        data class SetError(val isError: Boolean) : Message
    }

    sealed interface Action {
        data object LoadJoke : Action
    }
}
