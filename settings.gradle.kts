rootProject.name = "toir-mobile"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

includeBuild("build-logic")

// Android specific modules
include(":android:app")

// Shared common and core modules
include(":shared:main")

include(":shared:common")
include(":shared:common-ui")
include(":shared:common-resources")

include(":shared:core-domain")
include(":shared:core-mvikotlin")
include(":shared:core-presentation")
include(":shared:core-storage")
include(":shared:core-network")
include(":shared:core-navigation")
include(":shared:core-database")
include(":shared:core-auth")

// Shared feature modules

include(":shared:feature-auth:api")
include(":shared:feature-auth:impl")
include(":shared:feature-auth:presentation")
include(":shared:feature-auth:ui")

include(":shared:feature-demo-first:impl")
include(":shared:feature-demo-first:api")
include(":shared:feature-demo-first:presentation")
include(":shared:feature-demo-first:ui")

include(":shared:feature-demo-second:impl")
include(":shared:feature-demo-second:api")
include(":shared:feature-demo-second:presentation")
include(":shared:feature-demo-second:ui")
