// Load centralized dependency versions from platform (relative to root project)
apply(from = "${rootProject.projectDir}/platform/quarkus-platform.gradle")

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

// Retrieve version catalogue from platform file
val libs: Map<String, String> by extra
val versions: Map<String, String> by extra

dependencies {
    // Quarkus BOM — all Quarkus dependency versions are managed here
    implementation(enforcedPlatform(libs["quarkusBom"]!!))

    // Quarkus extensions (versions managed by BOM)
    implementation(libs["quarkusRest"]!!)
    implementation(libs["quarkusRestJackson"]!!)
    implementation(libs["quarkusArc"]!!)
    implementation(libs["quarkusKotlin"]!!)

    // Testing
    testImplementation(libs["quarkusJunit5"]!!)
    testImplementation(libs["restAssured"]!!)
}

group = "be.lutske"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
    kotlinOptions.javaParameters = true
}
