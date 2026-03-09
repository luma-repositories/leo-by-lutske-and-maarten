# Migrate Backend from Kotlin to Java 25

**Date:** 2026-03-09

## Summary

The Quarkus backend has been migrated from Kotlin to Java 25. All source files and tests have been rewritten in Java, using modern Java features such as records for DTOs. Kotlin plugins, dependencies, and configuration have been fully removed from the build.

## Changes

- **Source files**: Converted `VersionResource.kt` → `VersionResource.java`, `VersionResponse.kt` → `VersionResponse.java` (now a Java record)
- **Test files**: Converted `VersionResourceTest.kt` → `VersionResourceTest.java`, `HomePageAvailabilityTest.kt` → `HomePageAvailabilityTest.java`
- **build.gradle.kts**: Removed Kotlin plugins (`kotlin("jvm")`, `kotlin("plugin.allopen")`), removed `quarkus-kotlin` dependency, removed `allOpen` and `KotlinCompile` config blocks. Uses Java toolchain with language version 25.
- **platform/quarkus-platform.gradle**: Removed Kotlin version and `quarkusKotlin` library entry. Upgraded Quarkus from 3.17.7 to 3.32.2.
- **gradle.properties**: Removed `kotlin.code.style`. Updated `quarkusPluginVersion` to 3.32.2.
- **gradle-wrapper.properties**: Upgraded Gradle from 8.12 to 9.3.1 (required for Java 25 support).
- **README.md**: Updated prerequisites to Java 25, added full SDKMAN installation and configuration guide, updated project structure to reflect Java sources.

## Dependency/Version Changes

| Component | Before | After |
|-----------|--------|-------|
| Java | 21 | 25 |
| Gradle | 8.12 | 9.3.1 |
| Quarkus | 3.17.7 | 3.32.2 |
| Kotlin | 2.0.21 | Removed |

## User Impact

- Developers now need **Java 25** (OpenJDK 25) instead of Java 21
- SDKMAN installation instructions are provided in the README
- No API changes — all endpoints behave identically

## How to Verify

```bash
sdk install java 25.0.2-open
sdk use java 25.0.2-open
java -version          # Should show Java 25
./gradlew :backend:build   # Should compile and pass all tests
```
