package plugins

import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations
import extensions.kotlinMultiplatformConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class KmpLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureKotlin()
            configureDependencies()
            configureAndroid()
            configureIos()
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.kotlin.multiplatform.get().pluginId)
            apply(libs.plugins.kotlin.multiplatformAndroidLibrary.get().pluginId)
        }
    }

    private fun Project.configureKotlin() {
        kotlinMultiplatformConfig {
            jvmToolchain(libs.versions.javaVersion.get().toInt())

            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xcontext-parameters",
                    "-Xexpect-actual-classes",
                )
            }
        }
    }

    private fun Project.configureDependencies() {

        commonMainDependencies {
            implementations(
                libs.koin.core,
                libs.kotlin.coroutines.core,
                libs.kotlin.datetime,
                libs.napier,
            )
        }

        androidMainDependencies {
            implementations(
                libs.koin.android,
                libs.kotlin.coroutines.android,
            )
        }
    }

    private fun Project.configureAndroid() {
        androidLibraryConfig {
            compileSdk = libs.versions.android.compileSdk.get().toInt()
            minSdk = libs.versions.android.minSdk.get().toInt()

            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
            }
        }
    }

    private fun Project.configureIos() {
        kotlinMultiplatformConfig {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        }
    }
}
