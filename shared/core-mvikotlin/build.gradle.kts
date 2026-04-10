import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.core.mvikotlin"
}

commonMainDependencies {
    apis(
        libs.mvikotlin.core,
        libs.mvikotlin.coroutines,
        libs.mvikotlin.main,
        libs.mvikotlin.logging
    )
}
