package ru.mirea.toir.feature.auth.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.auth.model.DomainAuthUser
import ru.mirea.toir.core.auth.model.RefreshToken
import ru.mirea.toir.core.auth.repository.AuthRepository
import ru.mirea.toir.core.auth.storage.TokenStorage
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest

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

                    tokenStorage.saveTokens(
                        accessToken = response.accessToken.orEmpty(),
                        refreshToken = RefreshToken(response.refreshToken.orEmpty()),
                    )

                    userMapper.map(response.user ?: error("No user in login response")).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "login failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun getStoredAccessToken(): Result<String?> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = { tokenStorage.getAccessToken().wrapResultSuccess() },
                catchBlock = { throwable ->
                    Napier.e(message = "getStoredAccessToken failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun refreshAccessToken(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val refreshToken = tokenStorage.getRefreshToken()
                        ?: error("No refresh token stored")
                    val response = apiClient.refresh(refreshToken.value).getOrThrow()
                    tokenStorage.saveTokens(
                        accessToken = response.accessToken.orEmpty(),
                        refreshToken = RefreshToken(response.refreshToken.orEmpty()),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "refreshAccessToken failed", throwable = throwable)
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
}
