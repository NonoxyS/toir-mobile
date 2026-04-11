package ru.mirea.toir.feature.auth.impl.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ru.mirea.toir.core.auth.domain.models.AccessToken
import ru.mirea.toir.core.auth.domain.models.BearerTokens
import ru.mirea.toir.core.auth.domain.models.RefreshToken
import ru.mirea.toir.core.auth.data.storage.TokenStorage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TokenStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {

    override suspend fun saveTokens(bearerTokens: BearerTokens) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = bearerTokens.accessToken.value
            prefs[KEY_REFRESH_TOKEN] = bearerTokens.refreshToken.value
        }
    }

    override suspend fun getBearerTokens(): BearerTokens? {
        return dataStore.data.map { prefs ->
            val accessToken = prefs[KEY_ACCESS_TOKEN]
                ?.let(::AccessToken)
                ?: return@map null

            val refreshToken = prefs[KEY_REFRESH_TOKEN]
                ?.let(::RefreshToken)
                ?: return@map null

            BearerTokens(accessToken = accessToken, refreshToken = refreshToken)
        }.firstOrNull()
    }

    override suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getOrCreateDeviceCode(): String {
        val existing = dataStore.data.map { it[KEY_DEVICE_CODE] }.firstOrNull()
        if (existing != null) return existing
        val newCode = Uuid.random().toString()
        dataStore.edit { it[KEY_DEVICE_CODE] = newCode }
        return newCode
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val KEY_DEVICE_CODE = stringPreferencesKey("auth_device_code")
    }
}
