import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreAuth,
        projects.shared.coreNetwork,
        projects.shared.coreStorage,
        projects.shared.coreDatabase,
    )
}
