import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.presentation" }

commonMainDependencies {
    implementations(
        projects.shared.commonResources,
        libs.kotlin.immutableCollections,
    )
}
