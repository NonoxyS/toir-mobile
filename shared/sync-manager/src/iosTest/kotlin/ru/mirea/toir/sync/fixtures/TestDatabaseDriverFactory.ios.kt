package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual fun createInMemoryDriver(): SqlDriver = inMemoryDriver(ToirDatabase.Schema)
