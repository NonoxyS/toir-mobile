import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.sync"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
        projects.shared.coreAuth,
    )
}

androidMainDependencies {
    implementations(
        libs.androidx.workmanager,
        libs.koin.android.workmanager,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
        libs.ktor.clientMock,
        libs.ktor.contentNegotiation,
        libs.ktor.serializationJson,
        libs.sqldelight.native,
    )
}

// NativeSqliteDriver uses the sqliter cinterop which requires libsqlite3 at link time.
// Xcode provides this automatically for app targets; for standalone KN test binaries
// we must pass the flag explicitly.
kotlin {
    targets.withType<KotlinNativeTarget> {
        binaries.getTest("DEBUG").linkerOpts("-lsqlite3")
    }
}
