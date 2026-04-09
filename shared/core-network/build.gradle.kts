import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.apis
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations
import extensions.iosMainDependencies

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.core.network"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.coreDomain,

        libs.ktor.logging,
        libs.ktor.contentNegotiation,
        libs.ktor.serializationJson,
    )

    apis(
        libs.ktor.core,
    )
}

androidMainDependencies {
    apis(
        libs.ktor.engine.okhttp,
    )
}

iosMainDependencies {
    apis(
        libs.ktor.engine.darwin,
    )
}
