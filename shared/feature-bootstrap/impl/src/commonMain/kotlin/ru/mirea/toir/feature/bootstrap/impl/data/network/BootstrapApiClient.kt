package ru.mirea.toir.feature.bootstrap.impl.data.network

import ru.mirea.toir.feature.bootstrap.impl.data.network.models.RemoteBootstrapResponse

internal interface BootstrapApiClient {
    suspend fun fetchBootstrap(): Result<RemoteBootstrapResponse>
}
