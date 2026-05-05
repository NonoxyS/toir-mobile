package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.store.Bootstrapper
import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

/**
 * A [StoreFactory] for tests that always delegates [Store] creation with [autoInit] forced to
 * `false`, regardless of what the caller passes in.
 *
 * This lets tests subscribe to [Store.labels] and observe state before calling [Store.init]
 * manually, avoiding races where labels are emitted before the test's collector attaches —
 * without leaking a test-only `autoInit` parameter into the production [BootstrapStoreFactory] API.
 */
internal class TestStoreFactory(
    private val delegate: StoreFactory = DefaultStoreFactory(),
) : StoreFactory {

    override fun <Intent : Any, Action : Any, Message : Any, State : Any, Label : Any> create(
        name: String?,
        autoInit: Boolean,
        initialState: State,
        bootstrapper: Bootstrapper<Action>?,
        executorFactory: () -> Executor<Intent, Action, State, Message, Label>,
        reducer: Reducer<State, Message>,
    ): Store<Intent, State, Label> = delegate.create(
        name = name,
        autoInit = false,
        initialState = initialState,
        bootstrapper = bootstrapper,
        executorFactory = executorFactory,
        reducer = reducer,
    )
}
