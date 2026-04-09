package dev.nonoxy.kmmtemplate.core.network.ktor.certificates

import io.ktor.client.HttpClientConfig

internal expect fun HttpClientConfig<*>.configureCertificates()
