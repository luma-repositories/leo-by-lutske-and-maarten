plugins {
    java
    application
    id("io.quarkus.quarkus-plugin") version "3.15.0"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.quarkus.io") }
}

dependencies {
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-test-framework")
    
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}