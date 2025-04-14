rootProject.name = "storage-ms"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.gradle.plugin.management") version "0.13.1"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            credentials.username = providers.gradleProperty("authToken").get()
        }
    }
}