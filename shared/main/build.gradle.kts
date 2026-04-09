import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
    alias(libs.plugins.moko.resources)
}


androidLibraryConfig {
    namespace = "dev.nonoxy.kmmtemplate.main"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    addCommonModules()
    addCoreModules()
    addFeatureModules()
}

commonMainDependencies {
    apis(
        projects.shared.common,
        projects.shared.commonResources,
    )

    implementations(
        *composeBundle,
    )
}

private fun KotlinMultiplatformExtension.addFeatureModules() {

    val featureProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "feature-") }
        .values
        .flatMap { project -> project.childProjects.values }

    commonMainDependencies {
        implementations(*featureProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCoreModules() {

    val coreProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "core-") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*coreProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCommonModules() {

    val commonProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "common") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*commonProjects.toTypedArray())
    }
}

val changeGitHooksDir by tasks.registering(Exec::class) {
    group = "git"
    description = "Changing githooks dir to .githooks"

    commandLine("git", "config", "core.hooksPath", ".githooks")

    onlyIf {
        System.getenv("IS_CI") == null
    }
}

//tasks.getByPath(":shared:main:preBuild").dependsOn(changeGitHooksDir)