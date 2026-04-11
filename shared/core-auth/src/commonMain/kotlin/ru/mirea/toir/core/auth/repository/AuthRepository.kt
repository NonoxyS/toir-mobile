package ru.mirea.toir.core.auth.repository

import ru.mirea.toir.core.auth.model.DomainAuthUser

interface AuthRepository {
    suspend fun login(login: String, password: String): Result<DomainAuthUser>
    suspend fun getStoredAccessToken(): Result<String?>
    suspend fun refreshAccessToken(): Result<Unit>
    suspend fun logout(): Result<Unit>
}
