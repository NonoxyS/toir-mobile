package extensions

import org.gradle.api.Project

enum class FeatureModuleType {
    API,
    IMPL,
    PRESENTATION,
    UI;

    val actualName: String = name.lowercase()
}

val Project.isApiModule
    get() = name.lowercase() == FeatureModuleType.API.actualName

val Project.isImplModule
    get() = name.lowercase() == FeatureModuleType.IMPL.actualName

val Project.isPresentationModule
    get() = name.lowercase() == FeatureModuleType.PRESENTATION.actualName

val Project.isUiModule
    get() = name.lowercase() == FeatureModuleType.UI.actualName

fun Project.getApiModule(): Project? {
    return parent
        ?.childProjects
        ?.values
        ?.firstOrNull { project -> project.isApiModule }
}

fun Project.getPresentationModule(): Project? {
    return parent
        ?.childProjects
        ?.values
        ?.firstOrNull { project -> project.isPresentationModule }
}

internal fun <T> T.asList(): List<T> = listOf(this)