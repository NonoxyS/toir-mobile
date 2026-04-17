package ru.mirea.toir.core.auth.domain.models

data class DomainAuthUser(
    val id: String,
    val displayName: String,
    val role: UserRole,
)
