package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.Users

internal class UserDao(db: ToirDatabase) {
    private val queries = db.userQueries

    fun upsert(id: String, login: String, displayName: String, role: String) {
        queries.upsertUser(id = id, login = login, display_name = displayName, role = role)
    }

    fun selectAll(): List<Users> = queries.selectAll().executeAsList()

    fun selectById(id: String): Users? = queries.selectById(id).executeAsOneOrNull()

    fun deleteAll() = queries.deleteAll()
}
