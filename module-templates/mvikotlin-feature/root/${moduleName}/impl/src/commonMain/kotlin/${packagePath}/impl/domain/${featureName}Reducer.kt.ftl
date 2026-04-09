package ${packageName}.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ${packageName}.api.store.${featureName}Store.State
import ${packageName}.impl.domain.${featureName}StoreFactory.Message

internal class ${featureName}Reducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetLoading -> copy(
            isLoading = msg.isLoading,
        )

        is Message.SetError -> copy(
            isError = msg.isError,
        )
    }
}
