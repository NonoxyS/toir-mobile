package ru.mirea.toir.core.network.ktor.certificates

import io.ktor.client.HttpClientConfig

internal expect fun HttpClientConfig<*>.configureCertificates()
