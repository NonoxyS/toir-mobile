package ${packageName}.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ${packageName}.api.store.${featureName}Store.Intent
import ${packageName}.api.store.${featureName}Store.Label
import ${packageName}.api.store.${featureName}Store.State
import ${packageName}.impl.domain.${featureName}StoreFactory.Message
import dev.nonoxy.kmmtemplate.core.mvikotlin.BaseExecutor

internal class ${featureName}Executor(
    mainDispatcher: CoroutineDispatcher
): BaseExecutor<Intent, Nothing, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> handleLoadData()
        }
    }

    private suspend fun handleLoadData() {
        dispatch(Message.SetLoading(isLoading = true))
        // TODO: Implement your logic here
        dispatch(Message.SetLoading(isLoading = false))
    }
}
