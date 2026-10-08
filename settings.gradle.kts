pluginManagement {
    val quarkusPluginVersion: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version quarkusPluginVersion
    }
}

rootProject.name = "leo-legacy"

// Core compile-time modules for clean architecture
include(":core:domain")
include(":core:usecases")

// Existing backend module
include("backend")
