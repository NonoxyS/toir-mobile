package ru.mirea.toir.feature.bootstrap.impl.domain.repository

internal sealed interface BootstrapResult {
    data object Success : BootstrapResult
    data object Unauthorized : BootstrapResult
    data class Failure(val cause: Throwable) : BootstrapResult
}
