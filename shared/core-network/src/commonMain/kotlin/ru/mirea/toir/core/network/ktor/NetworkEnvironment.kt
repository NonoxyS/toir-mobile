package ru.mirea.toir.core.network.ktor

enum class NetworkEnvironment(
    val apiHost: String,
) {
    Dev(
        apiHost = "dev-back-template.com",
    ),
    Prod(
        apiHost = "prod-back-template.com",
    )
}
