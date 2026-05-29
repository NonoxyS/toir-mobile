package ru.mirea.toir.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlin.coroutines.cancellation.CancellationException

inline fun <T> Flow<T>.safeCatch(
    noinline action: suspend FlowCollector<T>.(cause: Throwable) -> Unit
): Flow<T> = catch { throwable ->
    if (throwable is CancellationException) throw throwable
    action(throwable)
}
