package ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures

import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

/** Returns a fixed [RemoteBootstrapResponse] without going through the network. */
internal class FakeBootstrapApiClient(
    private val response: RemoteBootstrapResponse,
) : BootstrapApiClient {

    var callCount: Int = 0
        private set

    override suspend fun fetchBootstrap(): Result<RemoteBootstrapResponse> {
        callCount++
        return Result.success(response)
    }
}
