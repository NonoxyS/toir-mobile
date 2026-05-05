package ru.mirea.toir.core.network.ktor

import io.ktor.http.URLProtocol

enum class NetworkEnvironment(
    val apiHost: String,
    val protocol: URLProtocol = URLProtocol.HTTPS,
) {
    Dev(apiHost = "82.25.58.221:8080", protocol = URLProtocol.HTTP),
    Prod(apiHost = "toir-backend.example.com"),
}
