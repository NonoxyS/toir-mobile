import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.ui" }

androidMainDependencies {
    implementations(libs.androidx.activity.compose)
}

commonMainDependencies {
    implementations(
        libs.coil.compose,
        libs.kotlin.immutableCollections,
    )
}
