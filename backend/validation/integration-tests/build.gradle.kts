plugins {
    java
    id("io.quarkus")
}

val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))

    implementation(project(":backend:application:core:domain"))
    implementation(project(":backend:application:core:ports"))
    implementation(project(":backend:application:core:usecases"))
    implementation(project(":backend:application:core:utils"))
    implementation(project(":backend:application:infrastructure:persistence:in-memory"))
    implementation(project(":backend:application:apis:jakarta-apis"))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}
