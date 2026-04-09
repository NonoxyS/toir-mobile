import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig {
    namespace = "ru.mirea.toir.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.material3
    )

    apis(
        libs.compose.multiplatform.navigation,
        libs.compose.multiplatform.backhandler,
    )
}
