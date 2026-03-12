# Add Quarkus plugin and BOM to build.gradle

## Related User Story

User Story: fix_quarkus_build

## Objective

Add the Quarkus Gradle plugin and BOM (Bill of Materials) to properly configure the Quarkus build system.

## Scope

- Add Quarkus Gradle plugin
- Add Quarkus BOM for dependency management
- Configure Quarkus platform version

## Out of Scope

- Dependency replacement (separate task)
- Application configuration
- Test configuration

## Implementation Details

### Add Quarkus Plugin and BOM

Update `backend/build.gradle`:

```gradle
plugins {
    id 'java'
    id 'io.quarkus' version '3.6.4'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation enforcedPlatform('io.quarkus.platform:quarkus-bom:3.6.4')
    // Dependencies will be added in separate task
}
```

### Expected Behavior

- Quarkus plugin should be available for build tasks
- Quarkus BOM should manage dependency versions
- Build should recognize Quarkus project structure

## Files / Modules Impacted

**Files to modify:**
- `backend/build.gradle` - plugins and platform sections only

## Acceptance Criteria

**Given** the Quarkus plugin is missing  
**When** the build.gradle is updated with Quarkus plugin  
**Then** Quarkus-specific gradle tasks should be available

**Given** Quarkus BOM is added  
**When** gradle build is executed  
**Then** Quarkus platform dependencies should be managed correctly

## Testing Requirements

**Verification Steps:**
- Run `./gradlew tasks` to verify Quarkus tasks are available (quarkusBuild, quarkusDev, etc.)
- Verify no plugin resolution errors
- Confirm BOM applies successfully