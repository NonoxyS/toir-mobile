package ru.mirea.toir.feature.bootstrap.impl.data.repository.fixtures

import app.cash.sqldelight.db.SqlDriver

internal expect fun createInMemoryDriver(): SqlDriver
