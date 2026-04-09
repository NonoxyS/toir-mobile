package ${packageName}.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ${packageName}.api.store.${featureName}Store
import ${packageName}.api.store.${featureName}Store.Intent
import ${packageName}.api.store.${featureName}Store.Label
import ${packageName}.api.store.${featureName}Store.State

internal class ${featureName}StoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
) {

    fun create(): ${featureName}Store =
        object :
            ${featureName}Store,
            Store<Intent, State, Label> by storeFactory.create(
                name = ${featureName}Store::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = {
                    ${featureName}Executor(
                        mainDispatcher = mainDispatcher
                    )
                },
                reducer = ${featureName}Reducer()
            ) {}

    sealed interface Message {
        data class SetLoading(val isLoading: Boolean) : Message
        data class SetError(val isError: Boolean) : Message
    }
}
