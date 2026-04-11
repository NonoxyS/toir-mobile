package ru.mirea.toir.core.auth.domain.repository

import ru.mirea.toir.core.auth.domain.models.BearerTokens
import ru.mirea.toir.core.auth.domain.models.DomainAuthUser

interface AuthRepository {
    suspend fun login(login: String, password: String): Result<DomainAuthUser>
    suspend fun getBearerTokens(): Result<BearerTokens?>
    suspend fun refreshBearerTokens(): Result<BearerTokens>
    suspend fun logout(): Result<Unit>
}
