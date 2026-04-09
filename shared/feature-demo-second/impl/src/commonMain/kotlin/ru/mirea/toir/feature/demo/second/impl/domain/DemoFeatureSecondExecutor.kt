package ru.mirea.toir.feature.demo.second.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Label
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.State
import ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action
import ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Message
import ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository
import ru.mirea.toir.core.mvikotlin.BaseExecutor

internal class DemoFeatureSecondExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val jokeRepository: ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository,
) : BaseExecutor<Intent, ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action, State, ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action) {
        when (action) {
            _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action.LoadJoke -> loadJoke()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.RefreshClick -> loadJoke()
        }
    }

    private suspend fun loadJoke() {
        dispatch(Message.SetLoading(true))
        dispatch(Message.SetError(false))
        jokeRepository.fetchJoke()
            .onSuccess { joke -> dispatch(Message.SetJoke(joke)) }
            .onFailure { dispatch(Message.SetError(true)) }
        dispatch(Message.SetLoading(false))
    }
}
