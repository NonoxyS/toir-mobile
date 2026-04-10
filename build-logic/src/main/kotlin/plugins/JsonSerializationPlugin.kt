package plugins

import extensions.commonMainDependencies
import extensions.dependencies
import extensions.implementation
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class JsonSerializationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.serialization.get().pluginId)

                when {
                    hasPlugin(libs.plugins.kotlin.multiplatform.get().pluginId) -> {
                        commonMainDependencies {
                            implementation(libs.kotlin.serialization.json)
                        }
                    }

                    hasPlugin(libs.plugins.androidLibrary.get().pluginId) ||
                        hasPlugin(libs.plugins.androidApplication.get().pluginId) -> {

                        dependencies {
                            implementation(libs.kotlin.serialization.json)
                        }
                    }
                }
            }
        }
    }
}
