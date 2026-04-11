package ru.mirea.toir.core.auth.storage

import ru.mirea.toir.core.auth.model.RefreshToken

interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: RefreshToken)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): RefreshToken?
    suspend fun clearTokens()
    suspend fun getOrCreateDeviceCode(): String
}
