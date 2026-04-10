package ru.mirea.toir.feature.auth.api.models

data class DomainAuthUser(
    val id: String,
    val displayName: String,
    val role: String,
)
