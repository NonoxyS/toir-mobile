package ru.mirea.toir.feature.auth.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.auth.domain.models.AccessToken
import ru.mirea.toir.core.auth.domain.models.BearerTokens
import ru.mirea.toir.core.auth.domain.models.DomainAuthUser
import ru.mirea.toir.core.auth.domain.models.RefreshToken
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.core.auth.data.storage.TokenStorage
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.models.request.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.response.RemoteRefreshTokensResponse

internal class AuthRepositoryImpl(
    private val apiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
    private val userMapper: AuthUserMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<DomainAuthUser> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val deviceCode = tokenStorage.getOrCreateDeviceCode()
                    val response = apiClient.login(
                        RemoteLoginRequest(
                            login = login,
                            password = password,
                            deviceCode = deviceCode,
                            platform = "android",
                            appVersion = null,
                        )
                    ).getOrThrow()

                    tokenStorage.saveTokens(bearerTokens = response.toBearerTokens())

                    userMapper.map(response.user).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "login failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun getBearerTokens(): Result<BearerTokens?> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = { tokenStorage.getBearerTokens().wrapResultSuccess() },
                catchBlock = { throwable ->
                    Napier.e(message = "getBearerTokens failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun refreshBearerTokens(): Result<BearerTokens> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val refreshToken = tokenStorage.getBearerTokens()?.refreshToken
                        ?: error("No refresh token stored")
                    val response = apiClient.refresh(refreshToken).getOrThrow()
                    val bearerTokens = response.toBearerTokens()
                    tokenStorage.saveTokens(bearerTokens)
                    bearerTokens.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "refreshBearerTokens failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun logout(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    tokenStorage.clearTokens()
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "logout failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    private fun RemoteRefreshTokensResponse.toBearerTokens(): BearerTokens {
        return BearerTokens(
            accessToken = AccessToken(accessToken),
            refreshToken = RefreshToken(refreshToken)
        )
    }

    private fun RemoteLoginResponse.toBearerTokens(): BearerTokens {
        return BearerTokens(
            accessToken = AccessToken(accessToken),
            refreshToken = RefreshToken(refreshToken)
        )
    }
}
