// Load centralized dependency versions from platform (relative to root project)
apply(from = "${rootProject.projectDir}/platform/quarkus-platform.gradle")

plugins {
    java
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

// Retrieve version catalogue from platform file
val libs: Map<String, String> by extra

dependencies {
    // Quarkus BOM — all Quarkus dependency versions are managed here
    implementation(enforcedPlatform(libs["quarkusBom"]!!))

    // Quarkus extensions (versions managed by BOM)
    implementation(libs["quarkusRest"]!!)
    implementation(libs["quarkusRestJackson"]!!)
    implementation(libs["quarkusArc"]!!)

    // Testing
    testImplementation(libs["quarkusJunit5"]!!)
    testImplementation(libs["restAssured"]!!)
}

group = "be.lutske"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
