import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.iosMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.sqldelight)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.core.database"
}

sqldelight {
    databases {
        create("ToirDatabase") {
            packageName.set("ru.mirea.toir.core.database")
            generateAsync = false
            dialect(libs.sqldelight.sqliteDialect)
        }
    }
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        libs.sqldelight.runtime,
        libs.sqldelight.coroutines,
        libs.sqldelight.primitiveAdapters,
    )
}

androidMainDependencies {
    implementations(
        libs.sqldelight.android,
        // Bundles SQLite ~3.49 into the APK so UPSERT (INSERT ... ON CONFLICT DO UPDATE,
        // requires SQLite >= 3.24) works on Android 8/9/10 — system SQLite there is 3.19/3.22.
        libs.requery.sqlite.android,
    )
}

iosMainDependencies {
    implementations(libs.sqldelight.native)
}
