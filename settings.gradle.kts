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

// Backend multi-module clean architecture
include("backend")
include("backend:application")
include("backend:application:core")
include("backend:application:core:utils")
include("backend:application:core:domain")
include("backend:application:core:ports")
include("backend:application:core:usecases")
include("backend:application:infrastructure")
include("backend:application:infrastructure:persistence")
include("backend:application:infrastructure:persistence:postgres")
include("backend:application:infrastructure:persistence:in-memory")
include("backend:application:infrastructure:gateways")
include("backend:application:infrastructure:gateways:http-clients")
include("backend:application:apis")
include("backend:application:apis:jakarta-apis")
include("backend:application:configuration")
include("backend:application:configuration:quarkus-app")
include("backend:validation")
include("backend:validation:architecture")
include("backend:validation:integration-tests")
