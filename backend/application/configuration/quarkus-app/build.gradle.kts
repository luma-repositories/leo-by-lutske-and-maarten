plugins {
    java
    id("io.quarkus")
}

val quarkusPlatformVersion: String by project
val langchain4jVersion: String by project

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))

    implementation(project(":backend:application:core:domain"))
    implementation(project(":backend:application:core:ports"))
    implementation(project(":backend:application:core:usecases"))
    implementation(project(":backend:application:core:utils"))
    implementation(project(":backend:application:infrastructure:persistence:postgres"))
    implementation(project(":backend:application:infrastructure:gateways:http-clients"))
    implementation(project(":backend:application:apis:jakarta-apis"))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    implementation("dev.langchain4j:langchain4j-open-ai:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langchain4jVersion")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
}

// Frontend build
val frontendDir = file("${rootProject.projectDir}/frontend")

val buildFrontend by tasks.registering(Exec::class) {
    description = "Build the React frontend (npm install + npm run build)"
    workingDir = frontendDir
    commandLine("bash", "-c", "npm install && npm run build")
    inputs.files(fileTree(frontendDir) {
        include("src/**", "public/**", "index.html", "package.json", "package-lock.json",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json", "vite.config.ts")
    })
    outputs.dir(file("${frontendDir}/dist"))
}

val copyFrontend by tasks.registering(Copy::class) {
    description = "Copy frontend build output into META-INF/resources for Quarkus"
    dependsOn(buildFrontend)
    from(file("${frontendDir}/dist"))
    into(layout.buildDirectory.dir("resources/main/META-INF/resources"))
}

tasks.named("processResources") {
    finalizedBy(copyFrontend)
}

tasks.named("jar") {
    dependsOn(copyFrontend)
}

tasks.named("classes") {
    dependsOn(copyFrontend)
}
