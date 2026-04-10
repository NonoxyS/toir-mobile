package ru.mirea.toir.feature.auth.impl.data.storage

internal interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun getOrCreateDeviceCode(): String
}
