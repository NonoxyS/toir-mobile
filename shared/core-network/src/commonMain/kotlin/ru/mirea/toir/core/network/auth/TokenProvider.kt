package ru.mirea.toir.core.network.auth

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun refreshAndGetAccessToken(): String?
}
