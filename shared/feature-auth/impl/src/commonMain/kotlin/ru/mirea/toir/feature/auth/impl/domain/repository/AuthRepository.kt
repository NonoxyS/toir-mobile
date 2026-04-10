package ru.mirea.toir.feature.auth.impl.domain.repository

import ru.mirea.toir.feature.auth.api.models.DomainAuthUser

internal interface AuthRepository {
    suspend fun login(login: String, password: String): Result<DomainAuthUser>
    suspend fun getStoredAccessToken(): String?
    suspend fun refreshAccessToken(): Result<Unit>
    suspend fun logout()
}
