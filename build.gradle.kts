plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.kotlin.cocoapods) apply false

    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatformAndroidLibrary) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.moko.resources) apply false

    alias(libs.plugins.detekt)

    // Convention plugins
    alias(libs.plugins.conventionPlugin.androidLibrary) apply false
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup) apply false
    alias(libs.plugins.conventionPlugin.kmpLibrary) apply false
    alias(libs.plugins.conventionPlugin.kmpLibraryLegacy) apply false
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup) apply false
    alias(libs.plugins.conventionPlugin.jsonSerialization) apply false
}

apply(from = "$rootDir/linters/androidLint/lintConfiguration.gradle")

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compose.kode)
    detektPlugins(libs.detekt.compose.twitter)
}

detekt {
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    description = "Detekt with formatting."
    baseline = file("$rootDir/linters/detekt/baseline.xml")
    config.setFrom(files("$rootDir/linters/detekt/config.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setupDetektFolders()

    reports {
        html {
            required.set(true)
            outputLocation.set(file("$rootDir/build/reports/detekt/detekt.html"))
        }
    }
}

fun SourceTask.setupDetektFolders() {
    setSource(files(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    exclude("**/.gradle/**")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
