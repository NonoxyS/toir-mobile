package ru.mirea.toir.feature.bootstrap.impl.data.network

import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

internal class BootstrapApiClientImpl(
    private val ktorClient: KtorClient,
) : BootstrapApiClient {

    override suspend fun fetchBootstrap(): Result<RemoteBootstrapResponse> =
        ktorClient.executeQuery(
            query = { ktorClient.get("/api/v1/mobile/bootstrap") },
            deserializer = RemoteBootstrapResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "fetchBootstrap failed",
        )
}
