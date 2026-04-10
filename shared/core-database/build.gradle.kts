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
        }
    }
}

commonMainDependencies {
    implementations(
        libs.sqldelight.runtime,
        libs.sqldelight.coroutines,
        libs.sqldelight.primitiveAdapters,
    )
}

androidMainDependencies {
    implementations(libs.sqldelight.android)
}

iosMainDependencies {
    implementations(libs.sqldelight.native)
}
