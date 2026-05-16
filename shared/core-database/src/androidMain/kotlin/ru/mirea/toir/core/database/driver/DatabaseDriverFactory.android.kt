package ru.mirea.toir.core.database.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Bundled SQLite ~3.49 через requery: системный на Android API 26-29 — 3.19/3.22, без
     * поддержки `INSERT ... ON CONFLICT DO UPDATE` (нужен с 3.24, используется в .sq merge-запросах).
     * iOS не затронут — нативный SQLite ≥ 3.40. APK +~1.5 МБ.
     */
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = ToirDatabase.Schema,
            context = context,
            name = "toir.db",
            factory = RequerySQLiteOpenHelperFactory(),
        )
}
