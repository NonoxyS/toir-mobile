package ru.mirea.toir.core.database.driver

import app.cash.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
