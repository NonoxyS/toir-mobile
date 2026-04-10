package ru.mirea.toir.core.network.ktor

import io.ktor.http.URLProtocol

enum class NetworkEnvironment(
    val apiHost: String,
    val protocol: URLProtocol = URLProtocol.HTTPS,
) {
    Dev(apiHost = "10.0.2.2:8080", protocol = URLProtocol.HTTP),
    Prod(apiHost = "toir-backend.example.com"),
}
