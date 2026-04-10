package ru.mirea.toir.feature.auth.impl.data.network

import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshResponse

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
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "auth login failed",
        )

    override suspend fun refresh(refreshToken: String): Result<RemoteRefreshResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoteRefreshRequest(refreshToken = refreshToken))
                }
            },
            deserializer = RemoteRefreshResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "auth refresh failed",
        )
}
