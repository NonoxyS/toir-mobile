package ru.mirea.toir.core.database.storage.user

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.Users
import ru.mirea.toir.core.database.storage.user.models.LocalUser

internal class UserStorageImpl(db: ToirDatabase) : UserStorage {

    private val queries = db.userQueries

    override fun upsert(id: String, login: String, displayName: String, role: String) {
        queries.upsertUser(
            id = id,
            login = login,
            display_name = displayName,
            role = role,
        )
    }

    override fun selectAll(): List<LocalUser> =
        queries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectById(id: String): LocalUser? =
        queries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun deleteAll() = queries.deleteAll()

    private fun Users.toLocal() = LocalUser(
        id = id,
        login = login,
        displayName = display_name,
        role = role,
    )
}
