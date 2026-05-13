@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.core.auth.data.storage.TokenStorage
import ru.mirea.toir.core.auth.domain.models.BearerTokens

class TestTokenStorage(
    private val deviceId: String? = "test-device-id",
) : TokenStorage {

    override suspend fun getDeviceId(): String? = deviceId

    override suspend fun saveTokens(bearerTokens: BearerTokens): Unit = Unit

    override suspend fun getBearerTokens(): BearerTokens? = null

    override suspend fun clearTokens(): Unit = Unit

    override suspend fun getOrCreateDeviceCode(): String = "test-device-code"

    override suspend fun saveDeviceId(deviceId: String): Unit = Unit
}
