package ru.mirea.toir.feature.bootstrap.impl.domain

import ru.mirea.toir.core.auth.domain.models.AccessToken
import ru.mirea.toir.core.auth.domain.models.BearerTokens
import ru.mirea.toir.core.auth.domain.models.DomainAuthUser
import ru.mirea.toir.core.auth.domain.models.RefreshToken
import ru.mirea.toir.core.auth.domain.repository.AuthRepository

internal class FakeAuthRepository(
    var tokens: BearerTokens? = BearerTokens(
        accessToken = AccessToken("access"),
        refreshToken = RefreshToken("refresh"),
    ),
    var logoutShouldThrow: Boolean = false,
) : AuthRepository {
    var logoutCallCount: Int = 0
        private set

    override suspend fun login(login: String, password: String): Result<DomainAuthUser> =
        Result.failure(NotImplementedError("FakeAuthRepository.login not implemented"))

    override suspend fun getBearerTokens(): Result<BearerTokens?> = Result.success(tokens)

    override suspend fun refreshBearerTokens(): Result<BearerTokens> =
        Result.failure(NotImplementedError("FakeAuthRepository.refreshBearerTokens not implemented"))

    override suspend fun logout(): Result<Unit> {
        logoutCallCount++
        if (logoutShouldThrow) {
            return Result.failure(RuntimeException("simulated logout failure"))
        }
        tokens = null
        return Result.success(Unit)
    }
}
