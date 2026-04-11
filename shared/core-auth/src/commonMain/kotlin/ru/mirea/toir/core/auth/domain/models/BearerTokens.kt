package ru.mirea.toir.core.auth.domain.models

data class BearerTokens(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
