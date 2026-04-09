package plugins

import extensions.androidConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations
import extensions.kotlinMultiplatformConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class KmpLibraryLegacyPlugin : Plugin<Project> {

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
            apply(libs.plugins.androidLibrary.get().pluginId)
        }
    }

    private fun Project.configureKotlin() {
        kotlinMultiplatformConfig {
            jvmToolchain(libs.versions.javaVersion.get().toInt())

            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
                }
            }

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
        androidConfig {
            compileSdk = libs.versions.android.compileSdk.get().toInt()

            defaultConfig {
                minSdk = libs.versions.android.minSdk.get().toInt()

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    isShrinkResources = false

                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility(libs.versions.javaVersion.get().toInt())
                targetCompatibility(libs.versions.javaVersion.get().toInt())
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
