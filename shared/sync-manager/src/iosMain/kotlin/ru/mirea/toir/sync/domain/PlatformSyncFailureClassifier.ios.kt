package ru.mirea.toir.sync.domain

internal actual fun platformClassifyFailure(throwable: Throwable): SyncFailureReason =
    SyncFailureReason.UNKNOWN
