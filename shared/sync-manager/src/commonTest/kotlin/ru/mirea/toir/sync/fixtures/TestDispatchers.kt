package ru.mirea.toir.sync.fixtures

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import ru.mirea.toir.common.coroutines.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
fun testDispatchers(): CoroutineDispatchers {
    val d: CoroutineDispatcher = UnconfinedTestDispatcher()
    return object : CoroutineDispatchers {
        override val io = d
        override val main = d
        override val default = d
        override val unconfined = d
    }
}
