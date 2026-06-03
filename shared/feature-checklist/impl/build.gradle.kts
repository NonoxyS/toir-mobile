import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.checklist.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
        projects.shared.coreDomain,
        libs.kotlin.datetime,
    )
}
