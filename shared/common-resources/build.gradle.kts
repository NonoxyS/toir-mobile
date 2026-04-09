import dev.icerock.gradle.MRVisibility
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.moko.resources)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.kmmtemplate.common.resources"
}

multiplatformResources {
    resourcesPackage.set("dev.nonoxy.kmmtemplate.res")
    iosBaseLocalizationRegion.set("en")
    resourcesClassName.set("MR")
    resourcesVisibility.set(MRVisibility.Public)
}

commonMainDependencies {
    apis(
        libs.moko.resources.core,
    )
}
