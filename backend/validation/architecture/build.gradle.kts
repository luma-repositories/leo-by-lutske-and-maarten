plugins {
    java
}

val archunitVersion: String by project

dependencies {
    testImplementation(project(":backend:application:core:domain"))
    testImplementation(project(":backend:application:core:ports"))
    testImplementation(project(":backend:application:core:usecases"))
    testImplementation(project(":backend:application:core:utils"))
    testImplementation(project(":backend:application:infrastructure:persistence:postgres"))
    testImplementation(project(":backend:application:infrastructure:persistence:in-memory"))
    testImplementation(project(":backend:application:infrastructure:gateways:http-clients"))
    testImplementation(project(":backend:application:apis:jakarta-apis"))
    testImplementation(project(":backend:application:configuration:quarkus-app"))

    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
