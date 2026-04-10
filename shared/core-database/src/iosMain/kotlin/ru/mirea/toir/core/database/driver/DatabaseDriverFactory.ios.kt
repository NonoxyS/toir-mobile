package ru.mirea.toir.core.database.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(
            schema = ToirDatabase.Schema,
            name = "toir.db",
        )
}
