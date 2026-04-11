package ru.mirea.toir.feature.auth.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.api.store.AuthStore.Intent
import ru.mirea.toir.feature.auth.api.store.AuthStore.Label
import ru.mirea.toir.feature.auth.api.store.AuthStore.State
import ru.mirea.toir.core.auth.domain.repository.AuthRepository

internal class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {

    fun create(): AuthStore =
        object :
            AuthStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = AuthStore::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = { AuthExecutor(authRepository, mainDispatcher) },
                reducer = AuthReducer(),
            ) {}

    internal sealed interface Message {
        data class SetLogin(val value: String) : Message
        data class SetPassword(val value: String) : Message
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data object ClearLoading : Message
    }
}
