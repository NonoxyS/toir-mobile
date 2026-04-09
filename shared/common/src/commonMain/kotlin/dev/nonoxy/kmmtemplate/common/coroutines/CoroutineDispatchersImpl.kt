package dev.nonoxy.kmmtemplate.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher

internal class CoroutineDispatchersImpl : CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = ioDispatcher

    override val main: CoroutineDispatcher
        get() = mainDispatcher

    override val default: CoroutineDispatcher
        get() = defaultDispatcher

    override val unconfined: CoroutineDispatcher
        get() = unconfinedDispatcher
}
