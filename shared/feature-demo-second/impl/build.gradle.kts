import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.kmmtemplate.feature.demo.second.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
    )
}
