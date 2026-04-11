package ru.mirea.toir.feature.auth.impl.data.network

import ru.mirea.toir.core.auth.domain.models.RefreshToken
import ru.mirea.toir.feature.auth.impl.data.network.models.request.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteRefreshTokensResponse

internal interface AuthApiClient {
    suspend fun login(request: RemoteLoginRequest): Result<RemoteLoginResponse>
    suspend fun refresh(refreshToken: RefreshToken): Result<RemoteRefreshTokensResponse>
}
