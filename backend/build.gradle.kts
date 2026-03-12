plugins {
    java
    id("io.quarkus")
}

// Apply the centralized platform configuration
apply(from = "../platform/quarkus-platform.gradle")

repositories {
    mavenCentral()
}

// Access the centralized dependency definitions
val libs = project.extra["libs"] as Map<String, String>

dependencies {
    // Import Quarkus BOM
    implementation(enforcedPlatform(libs["quarkusBom"]!!))
    
    // Core Quarkus extensions
    implementation(libs["quarkusRest"]!!)
    implementation(libs["quarkusRestJackson"]!!)
    implementation(libs["quarkusArc"]!!)
    
    // Database
    implementation(libs["quarkusHibernateOrm"]!!)
    implementation(libs["quarkusJdbcPostgresql"]!!)
    implementation(libs["quarkusFlyway"]!!)
    
    // LangChain4j
    implementation(libs["langchain4jOpenai"]!!)
    implementation(libs["langchain4jAnthropic"]!!)
    
    // Test dependencies
    testImplementation(libs["quarkusJunit5"]!!)
    testImplementation(libs["quarkusMockito"]!!)
    testImplementation(libs["quarkusTestH2"]!!)
    testImplementation(libs["restAssured"]!!)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}