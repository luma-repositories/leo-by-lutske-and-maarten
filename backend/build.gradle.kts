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

    // Database
    implementation(libs["quarkusHibernateOrm"]!!)
    implementation(libs["quarkusJdbcPostgresql"]!!)
    implementation(libs["quarkusFlyway"]!!)

    // OCR
    implementation(libs["tess4j"]!!)

    // Testing
    testImplementation(libs["quarkusJunit5"]!!)
    testImplementation(libs["quarkusMockito"]!!)
    testImplementation(libs["quarkusTestH2"]!!)
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
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// ---------------------------------------------------------------------------
// Frontend build: npm install + build, then copy dist → META-INF/resources
// ---------------------------------------------------------------------------
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
