package ru.mirea.toir.sync.domain

import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

internal actual fun platformClassifyFailure(throwable: Throwable): SyncFailureReason = when (throwable) {
    is UnknownHostException -> SyncFailureReason.NETWORK
    is ConnectException -> SyncFailureReason.NETWORK
    is SocketException -> SyncFailureReason.NETWORK
    else -> SyncFailureReason.UNKNOWN
}
