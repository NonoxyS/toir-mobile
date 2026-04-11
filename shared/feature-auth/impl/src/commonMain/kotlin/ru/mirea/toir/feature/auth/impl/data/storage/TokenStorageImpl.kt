package ru.mirea.toir.feature.auth.impl.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ru.mirea.toir.core.auth.model.RefreshToken
import ru.mirea.toir.core.auth.storage.TokenStorage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TokenStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {

    override suspend fun saveTokens(accessToken: String, refreshToken: RefreshToken) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken.value
        }
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()

    override suspend fun getRefreshToken(): RefreshToken? =
        dataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()?.let(::RefreshToken)

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
