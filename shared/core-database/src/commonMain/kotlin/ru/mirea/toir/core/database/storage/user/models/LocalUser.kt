package ru.mirea.toir.core.database.storage.user.models

data class LocalUser(
    val id: String,
    val login: String,
    val displayName: String,
    val role: String,
)
