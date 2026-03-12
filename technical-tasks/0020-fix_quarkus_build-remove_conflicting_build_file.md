# Remove conflicting build.gradle file

## Related User Story

User Story: fix_quarkus_build

## Objective

Remove the conflicting `build.gradle` file that contains Spring Boot dependencies, allowing Gradle to use the correct `build.gradle.kts` file with proper Quarkus configuration.

## Scope

- Remove `backend/build.gradle` file
- Ensure Gradle uses `backend/build.gradle.kts` 
- Verify correct build configuration is active

## Out of Scope

- Modifying build.gradle.kts (it's already correct)
- Adding new dependencies
- Application code changes

## Implementation Details

### Problem Analysis

The project has **two build files**:
- `backend/build.gradle` - Contains **wrong** Spring Boot dependencies
- `backend/build.gradle.kts` - Contains **correct** Quarkus configuration

Gradle is using the `.gradle` file instead of the `.gradle.kts` file, causing build failures.

### Solution

**Remove the conflicting file:**
```bash
cd backend
rm build.gradle
```

### Current Correct Configuration

The `build.gradle.kts` file already contains all required dependencies:

```kotlin
plugins {
    java
    application
    id("io.quarkus.quarkus-plugin") version "3.15.0"
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
```

## Files / Modules Impacted

**Files to remove:**
- `backend/build.gradle` - DELETE this file

**Files to keep:**
- `backend/build.gradle.kts` - This is the correct configuration

## Acceptance Criteria

**Given** two conflicting build files exist  
**When** the incorrect build.gradle is removed  
**Then** only build.gradle.kts should remain

**Given** build.gradle is removed  
**When** gradle commands are executed  
**Then** Gradle should use build.gradle.kts configuration

**Given** correct build file is active  
**When** dependencies are resolved  
**Then** Quarkus extensions should be available (not Spring Boot)

## Testing Requirements

**Verification Steps:**
- Confirm `backend/build.gradle` file is deleted
- Confirm `backend/build.gradle.kts` file still exists
- Run `./gradlew tasks` to verify Quarkus tasks are available
- Verify no Spring Boot dependencies in resolved tree