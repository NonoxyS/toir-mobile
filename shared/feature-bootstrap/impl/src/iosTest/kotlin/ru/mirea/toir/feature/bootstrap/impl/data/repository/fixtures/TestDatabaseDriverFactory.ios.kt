package ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual fun createInMemoryDriver(): SqlDriver = inMemoryDriver(ToirDatabase.Schema)
