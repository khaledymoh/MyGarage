pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "MyGarage"

include(":app")
include(":core")
include(":core:domain")
include(":core:presentation")
include(":core:data")
include(":resources")
include(":authentication", ":authentication:api")
include(":welcome", ":welcome:api")
include(":garage", ":garage:api")
include(":vehicle", ":vehicle:api")
include(":maintenance", ":maintenance:api")
include(":settings", ":settings:api")
