package ru.mirea.toir.feature.demo.second.impl.data.network

import ru.mirea.toir.feature.demo.second.impl.data.network.models.RemoteJokeResponse
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient

internal class JokeApiClientImpl(
    private val ktorClient: KtorClient,
) : ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient {

    override suspend fun fetchJoke(): Result<ru.mirea.toir.feature.demo.second.impl.data.network.models.RemoteJokeResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.get(
                    _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient.Companion.URL_JOKE
                )
            },
            deserializer = _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.data.network.models.RemoteJokeResponse.serializer(),
            success = { dto -> dto.wrapResultSuccess() },
            loggingErrorMessage = "JokeApiClientImpl: failed to fetch joke",
        )
}
