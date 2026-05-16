import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.bootstrap.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
        projects.shared.coreAuth,
        projects.shared.syncManager,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
        libs.sqldelight.native,
    )
}

// NativeSqliteDriver needs libsqlite3 at link time for KN test binaries
// (same as :shared:sync-manager build.gradle.kts).
kotlin {
    targets.withType<KotlinNativeTarget> {
        binaries.getTest("DEBUG").linkerOpts("-lsqlite3")
    }
}
