import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.core.auth"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
    )
}
