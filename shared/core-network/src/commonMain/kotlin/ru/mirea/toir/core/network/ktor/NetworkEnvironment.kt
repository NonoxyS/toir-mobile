package ru.mirea.toir.core.network.ktor

import io.ktor.http.URLProtocol

enum class NetworkEnvironment(
    val apiHost: String,
    val protocol: URLProtocol = URLProtocol.HTTPS,
) {
    Dev(apiHost = "toir-api.nonoxy.dev"),
    Prod(apiHost = "toir-api.nonoxy.dev"),
}
