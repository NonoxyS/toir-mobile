package ru.mirea.toir.feature.demo.first.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.Label
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.State
import ru.mirea.toir.feature.demo.first.impl.domain.DemoFeatureFirstStoreFactory.Message
import ru.mirea.toir.core.mvikotlin.BaseExecutor

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
