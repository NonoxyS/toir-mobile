package ru.mirea.toir.core.auth.token

import ru.mirea.toir.core.auth.repository.AuthRepository
import ru.mirea.toir.core.auth.storage.TokenStorage

internal class TokenProviderImpl(
    private val tokenStorage: TokenStorage,
    private val authRepository: AuthRepository,
) : TokenProvider {

    override suspend fun getAccessToken(): String? = tokenStorage.getAccessToken()

    override suspend fun refreshAndGetAccessToken(): String? =
        authRepository.refreshAccessToken()
            .fold(
                onSuccess = { tokenStorage.getAccessToken() },
                onFailure = { null },
            )
}
