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
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("dev.langchain4j:langchain4j-chat-models:0.23.0")
    implementation("dev.langchain4j:langchain4j-data:0.23.0")
    implementation("org.jboss.logging:jboss-logging:3.4.1.Final")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-test-framework")
    
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}