package ru.mirea.toir.core.auth.token

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun refreshAndGetAccessToken(): String?
}
