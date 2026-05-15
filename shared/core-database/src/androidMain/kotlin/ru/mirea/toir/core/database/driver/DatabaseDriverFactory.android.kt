package ru.mirea.toir.core.database.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Использует requery's bundled SQLite (~3.49) вместо системного. На Android API 26-29
     * (Android 8/9/10) системный SQLite — 3.19/3.22, в нём нет поддержки `INSERT ... ON
     * CONFLICT DO UPDATE` (UPSERT, доступен с 3.24). Эти запросы есть в `Inspection.sq`,
     * `InspectionEquipmentResult.sq`, `ChecklistItemResult.sq`, `Photo.sq` — мёрж-правило
     * Waypoint 11 §1.3. Без bundle падало бы в рантайме на старых устройствах.
     * APK тяжелее на ~1.5 МБ; iOS не затронут — там нативный SQLite ≥ 3.40.
     */
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = ToirDatabase.Schema,
            context = context,
            name = "toir.db",
            factory = RequerySQLiteOpenHelperFactory(),
        )
}
