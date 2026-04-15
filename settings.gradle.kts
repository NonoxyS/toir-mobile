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

include(":shared:feature-bootstrap:api")
include(":shared:feature-bootstrap:impl")
include(":shared:feature-bootstrap:presentation")
include(":shared:feature-bootstrap:ui")

include(":shared:feature-routes-list:api")
include(":shared:feature-routes-list:impl")
include(":shared:feature-routes-list:presentation")
include(":shared:feature-routes-list:ui")
