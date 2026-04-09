package dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.Label
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.State
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Message
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository
import dev.nonoxy.kmmtemplate.core.mvikotlin.BaseExecutor

internal class DemoFeatureSecondExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val jokeRepository: dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository,
) : BaseExecutor<Intent, dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action, State, dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action) {
        when (action) {
            _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory.Action.LoadJoke -> loadJoke()
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
