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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CakeDayy"

include(":app")
include(":core:model")
include(":core:domain")
include(":core:common")
include(":core:testing")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:notifications")
include(":core:designsystem")
include(":core:ui")
include(":feature:people")
include(":feature:editperson")
include(":feature:groups")
include(":feature:settings")
include(":widget")