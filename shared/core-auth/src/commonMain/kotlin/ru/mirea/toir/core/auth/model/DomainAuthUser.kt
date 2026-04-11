package ru.mirea.toir.core.auth.model

data class DomainAuthUser(
    val id: String,
    val displayName: String,
    val role: String,
)
