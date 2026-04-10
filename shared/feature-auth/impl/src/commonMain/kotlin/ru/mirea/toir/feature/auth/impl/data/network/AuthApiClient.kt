package ru.mirea.toir.feature.auth.impl.data.network

import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshResponse

internal interface AuthApiClient {
    suspend fun login(request: RemoteLoginRequest): Result<RemoteLoginResponse>
    suspend fun refresh(refreshToken: String): Result<RemoteRefreshResponse>
}
