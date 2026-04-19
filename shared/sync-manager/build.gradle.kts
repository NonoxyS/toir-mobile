import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.sync"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
        projects.shared.coreAuth,
    )
}

androidMainDependencies {
    implementations(
        libs.androidx.workmanager,
        libs.koin.android.workmanager,
    )
}
