# Migrate Backend from Kotlin to Java 25

**Date:** 2026-03-09

## Summary

The Quarkus backend has been fully migrated from Kotlin to Java 25. All source files — entities, repositories, REST resources, DTOs, and tests — have been rewritten in Java, using modern features such as records for DTOs. Kotlin plugins, dependencies, and configuration have been completely removed.

## Changes

- **Entities**: `RecipeEntity.kt` → `RecipeEntity.java`, `CategoryEntity.kt` → `CategoryEntity.java` (plain JPA entities)
- **Repositories**: `RecipeRepository.kt` → `RecipeRepository.java`, `CategoryRepository.kt` → `CategoryRepository.java` (Java Panache)
- **REST resources**: `RecipeResource.kt` → `RecipeResource.java`, `CategoryResource.kt` → `CategoryResource.java`, `VersionResource.kt` → `VersionResource.java`
- **DTOs**: All Kotlin `data class` → Java `record` (`RecipeDetailResponse`, `RecipeSummaryResponse`, `CategoryResponse`, `VersionResponse`)
- **Tests**: All Kotlin tests → Java (`RecipeResourceTest`, `CategoryResourceTest`, `VersionResourceTest`)
- **Removed**: `HomePageAvailabilityTest` (static site no longer exists in webapp branch)
- **platform/quarkus-platform.gradle**: Changed `quarkus-hibernate-orm-panache-kotlin` → `quarkus-hibernate-orm-panache`
- **build.gradle.kts**: Added database dependencies, Java 25 toolchain, removed all Kotlin config
- **Gradle**: 8.12 → 9.3.1 (required for Java 25 support)
- **Quarkus**: 3.17.7 → 3.32.2 (required for Java 25 support)
- **README.md**: Full webapp documentation — database setup, compose, frontend, all API endpoints, SDKMAN guide, tech stack

## Dependency/Version Changes

| Component | Before | After |
|-----------|--------|-------|
| Java | 21 (Kotlin) | 25 (Java) |
| Gradle | 8.12 | 9.3.1 |
| Quarkus | 3.17.7 | 3.32.2 |
| Panache | panache-kotlin | panache (Java) |
| Kotlin | 2.0.21 | Removed |

## User Impact

- Developers now need **Java 25** (OpenJDK 25) instead of Java 21
- SDKMAN installation instructions are provided in the README
- No API changes — all endpoints behave identically

## How to Verify

```bash
sdk use java 25.0.2-open
./gradlew :backend:build    # All tests pass
cd frontend && npm run test  # All tests pass
```
