package ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import ru.mirea.toir.common.coroutines.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
internal fun testDispatchers(): CoroutineDispatchers {
    val d: CoroutineDispatcher = UnconfinedTestDispatcher()
    return object : CoroutineDispatchers {
        override val io = d
        override val main = d
        override val default = d
        override val unconfined = d
    }
}
