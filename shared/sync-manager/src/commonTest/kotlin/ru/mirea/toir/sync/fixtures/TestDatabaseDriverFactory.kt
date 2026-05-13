package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver

internal expect fun createInMemoryDriver(): SqlDriver
