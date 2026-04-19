import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }

androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.impl" }

commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
    )
}
