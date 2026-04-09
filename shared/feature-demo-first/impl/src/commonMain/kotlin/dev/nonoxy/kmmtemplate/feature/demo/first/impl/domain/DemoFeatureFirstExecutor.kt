package dev.nonoxy.kmmtemplate.feature.demo.first.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.Label
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.State
import dev.nonoxy.kmmtemplate.feature.demo.first.impl.domain.DemoFeatureFirstStoreFactory.Message
import dev.nonoxy.kmmtemplate.core.mvikotlin.BaseExecutor

internal class DemoFeatureFirstExecutor(
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Nothing, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnNumberValueChange -> dispatch(Message.SetNumberValue(intent.newValue))
            Intent.NextButtonClick -> publish(Label.NavigateToSecondScreen)
        }
    }
}
