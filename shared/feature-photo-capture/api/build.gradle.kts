import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }

androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.api" }

commonMainDependencies {
    implementations(libs.kotlin.immutableCollections)
}
