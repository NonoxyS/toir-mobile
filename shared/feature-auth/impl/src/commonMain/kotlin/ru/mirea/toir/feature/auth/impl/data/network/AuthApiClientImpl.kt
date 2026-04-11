package ru.mirea.toir.feature.auth.impl.data.network

import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.auth.domain.models.RefreshToken
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.feature.auth.impl.data.network.models.request.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.request.RemoteRefreshTokensRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteRefreshTokensResponse

internal class AuthApiClientImpl(
    private val ktorClient: KtorClient,
) : AuthApiClient {

    override suspend fun login(request: RemoteLoginRequest): Result<RemoteLoginResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            deserializer = RemoteLoginResponse.serializer(),
            success = { response -> response.wrapResultSuccess() },
            loggingErrorMessage = "auth login failed",
        )

    override suspend fun refresh(refreshToken: RefreshToken): Result<RemoteRefreshTokensResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoteRefreshTokensRequest(refreshToken = refreshToken.value))
                }
            },
            deserializer = RemoteRefreshTokensResponse.serializer(),
            success = { response -> response.wrapResultSuccess() },
            loggingErrorMessage = "auth refresh failed",
        )
}
