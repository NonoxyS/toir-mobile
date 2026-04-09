import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.kmmtemplate.core.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.common,

        libs.mvikotlin.core,
        libs.mvikotlin.coroutines,
    )

    apis(
        libs.moko.mvvm.flow,
        libs.androidx.lifecycle.viewmodel,
    )
}
