package ru.mirea.toir.core.auth.domain.models

enum class UserRole {
    EXECUTOR,
    ADMIN,
    UNKNOWN,
    ;

    companion object {
        fun fromString(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
