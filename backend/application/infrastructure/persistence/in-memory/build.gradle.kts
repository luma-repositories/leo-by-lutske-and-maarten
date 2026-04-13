plugins {
    `java-library`
}

dependencies {
    implementation(project(":backend:application:core:domain"))
    implementation(project(":backend:application:core:ports"))
}
