package ru.mirea.toir.core.database.storage.user

import ru.mirea.toir.core.database.storage.user.models.LocalUser

interface UserStorage {

    fun upsert(
        id: String,
        login: String,
        displayName: String,
        role: String,
    )

    fun selectAll(): List<LocalUser>

    fun selectById(id: String): LocalUser?

    fun deleteAll()
}
