import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.equipment.card.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.commonResources,
    )
}
