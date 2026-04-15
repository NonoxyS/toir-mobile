import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.equipment.card.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
    )
}
