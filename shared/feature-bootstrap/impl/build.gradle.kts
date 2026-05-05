import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
        projects.shared.coreAuth,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.coroutines.test,
    )
}
