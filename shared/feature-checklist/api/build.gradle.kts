import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.apis

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.checklist.api"
}

commonMainDependencies {
    apis(
        libs.kotlin.immutableCollections,
    )
}
