package ru.mirea.toir.sync.domain

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException

internal fun Throwable.toSyncFailureReason(): SyncFailureReason = when {
    this is ResponseException && response.status == HttpStatusCode.Unauthorized -> SyncFailureReason.AUTH
    this is ResponseException && response.status.value in 500..599 -> SyncFailureReason.SERVER
    this is UnresolvedAddressException -> SyncFailureReason.NETWORK
    this is ConnectTimeoutException -> SyncFailureReason.NETWORK
    this is SocketTimeoutException -> SyncFailureReason.NETWORK
    this is HttpRequestTimeoutException -> SyncFailureReason.NETWORK
    this is IOException -> SyncFailureReason.NETWORK
    else -> SyncFailureReason.UNKNOWN
}
