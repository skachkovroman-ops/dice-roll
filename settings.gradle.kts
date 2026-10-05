pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // В Gradle DSL строка "PREFER_SETTINGS" или "FAIL_ON_PROJECT_REPOSITORIES" автоматически кастится в enum
    repositoriesMode.set(org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOSITORIES)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Dice Roller"
include(":app")
