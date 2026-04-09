package plugins

import extensions.androidConfig
import extensions.androidKotlinConfig
import extensions.kotlinJvmCompilerOptions
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureKotlin()
            configureAndroid()
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.androidLibrary.get().pluginId)
            apply(libs.plugins.kotlin.android.get().pluginId)
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

    private fun Project.configureKotlin() {
        androidKotlinConfig {
            jvmToolchain(libs.versions.javaVersion.get().toInt())
        }

        kotlinJvmCompilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
        }
    }
}
