package ru.mirea.toir.sync.domain

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException

internal fun Throwable.toSyncFailureReason(): SyncFailureReason = when {
    this is ResponseException && response.status == HttpStatusCode.Unauthorized -> SyncFailureReason.AUTH
    this is ResponseException && response.status.value in 500..599 -> SyncFailureReason.SERVER
    this is UnresolvedAddressException -> SyncFailureReason.NETWORK
    this is ConnectTimeoutException -> SyncFailureReason.NETWORK
    this is SocketTimeoutException -> SyncFailureReason.NETWORK
    this is HttpRequestTimeoutException -> SyncFailureReason.NETWORK
    else -> platformClassifyFailure(this)
}

/**
 * Platform fallback so we can keep classifying real network errors (`SocketException`,
 * `UnknownHostException`, …) on the JVM while not lumping local IO errors like
 * `FileNotFoundException` into NETWORK — both inherit from `IOException`, which is why
 * the previous blanket `IOException -> NETWORK` rule produced the "no connection"
 * message for a missing-photo upload.
 */
internal expect fun platformClassifyFailure(throwable: Throwable): SyncFailureReason
