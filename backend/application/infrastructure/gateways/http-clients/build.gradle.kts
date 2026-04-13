plugins {
    `java-library`
}

val quarkusPlatformVersion: String by project
val langchain4jVersion: String by project

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))

    implementation(project(":backend:application:core:domain"))
    implementation(project(":backend:application:core:ports"))

    implementation("dev.langchain4j:langchain4j-open-ai:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langchain4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.jboss.logging:jboss-logging")
}
