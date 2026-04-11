package ru.mirea.toir.core.auth.data.storage

import ru.mirea.toir.core.auth.domain.models.BearerTokens

interface TokenStorage {
    suspend fun saveTokens(bearerTokens: BearerTokens)
    suspend fun getBearerTokens(): BearerTokens?
    suspend fun clearTokens()
    suspend fun getOrCreateDeviceCode(): String
}
