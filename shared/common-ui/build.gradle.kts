import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.common.ui"

    lint {
        // Disabled due to lint bug with nullable Composable types in ModalBottomSheetConfiguration.kt
        disable.addAll(
            listOf(
                "ModifierFactoryExtensionFunction",
                "ModifierFactoryReturnType",
                "ModifierFactoryUnreferencedReceiver"
            )
        )
    }
}

commonMainDependencies {
    implementations(
        projects.shared.commonResources,

        *composeBundle,
        libs.moko.resources.compose
    )
}
