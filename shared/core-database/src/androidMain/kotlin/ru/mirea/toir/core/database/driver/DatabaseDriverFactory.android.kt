package ru.mirea.toir.core.database.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = ToirDatabase.Schema,
            context = context,
            name = "toir.db",
        )
}
