package dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network

import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models.RemoteJokeResponse
import dev.nonoxy.kmmtemplate.common.extensions.wrapResultSuccess
import dev.nonoxy.kmmtemplate.core.network.ktor.KtorClient

internal class JokeApiClientImpl(
    private val ktorClient: KtorClient,
) : dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient {

    override suspend fun fetchJoke(): Result<dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models.RemoteJokeResponse> =
        ktorClient.executeQuery(
            query = { ktorClient.get(_root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient.Companion.URL_JOKE) },
            deserializer = _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models.RemoteJokeResponse.serializer(),
            success = { dto -> dto.wrapResultSuccess() },
            loggingErrorMessage = "JokeApiClientImpl: failed to fetch joke",
        )
}
