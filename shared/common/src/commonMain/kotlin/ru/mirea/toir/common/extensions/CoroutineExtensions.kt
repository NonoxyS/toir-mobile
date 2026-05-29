package ru.mirea.toir.common.extensions

import kotlin.coroutines.cancellation.CancellationException

// https://detekt.dev/docs/rules/coroutines/#suspendfunswallowedcancellation
inline fun <T, R> T.coRunCatching(
    tryBlock: () -> R,
    catchBlock: (Throwable) -> R,
    finallyBlock: () -> Unit = {}
): R {
    return try {
        tryBlock()
    } catch (throwable: Throwable) {
        when (throwable) {
            is CancellationException -> throw throwable
            else -> catchBlock(throwable)
        }
    } finally {
        finallyBlock()
    }
}
