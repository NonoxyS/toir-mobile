package ru.mirea.toir.common.utils

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
data class OneTimeGetValue<T>(
    val defaultValue: T,
) {
    private var _value: AtomicReference<T?> = AtomicReference(value = null)

    fun readAndClear(): T = _value.exchange(newValue = null) ?: defaultValue

    fun setValue(value: T) = _value.store(newValue = value)
}
