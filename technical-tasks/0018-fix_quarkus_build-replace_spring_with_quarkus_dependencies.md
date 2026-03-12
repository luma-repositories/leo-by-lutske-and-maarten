# Replace Spring Boot dependencies with Quarkus equivalents

## Related User Story

User Story: fix_quarkus_build

## Objective

Replace all Spring Boot dependencies with their Quarkus equivalents to fix build errors and align with Quarkus framework.

## Scope

- Remove all Spring Boot dependencies
- Add Quarkus REST (JAX-RS) extension
- Add Quarkus Hibernate ORM with Panache extension
- Add Quarkus JDBC PostgreSQL extension

## Out of Scope

- Plugin configuration (separate task)
- Test dependencies (separate task)
- Application code changes

## Implementation Details

### Remove Spring Dependencies and Add Quarkus Extensions

Update `backend/build.gradle` dependencies section:

**Remove these Spring Boot dependencies:**
```gradle
// REMOVE ALL OF THESE:
implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.1.5'
implementation 'org.springframework.boot:spring-boot-starter-web:3.1.5'
implementation 'org.hibernate:hibernate-core:6.2.5.Final'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.hibernate:hibernate-core'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

**Add these Quarkus extensions:**
```gradle
dependencies {
    implementation enforcedPlatform('io.quarkus.platform:quarkus-bom:3.6.4')
    
    // Core Quarkus extensions
    implementation 'io.quarkus:quarkus-resteasy-reactive-jackson'
    implementation 'io.quarkus:quarkus-hibernate-orm-panache'
    implementation 'io.quarkus:quarkus-jdbc-postgresql'
    implementation 'io.quarkus:quarkus-flyway'
    
    // Test dependencies (keep mockito for now)
    testImplementation 'io.quarkus:quarkus-junit5'
    testImplementation 'org.mockito:mockito-core:3.12.4'
}
```

### Dependency Mapping

| Spring Boot | Quarkus Equivalent |
|-------------|-------------------|
| spring-boot-starter-web | quarkus-resteasy-reactive-jackson |
| spring-boot-starter-data-jpa | quarkus-hibernate-orm-panache |
| hibernate-core | Included in hibernate-orm-panache |
| spring-boot-starter-test | quarkus-junit5 |

## Files / Modules Impacted

**Files to modify:**
- `backend/build.gradle` - dependencies section only

## Acceptance Criteria

**Given** Spring Boot dependencies exist in build.gradle  
**When** they are replaced with Quarkus equivalents  
**Then** no Spring Boot dependencies should remain

**Given** Quarkus extensions are added  
**When** build is executed  
**Then** all required Quarkus functionality should be available

**Given** dependency versions are managed by Quarkus BOM  
**When** dependencies are resolved  
**Then** compatible versions should be selected automatically

## Testing Requirements

**Verification Steps:**
- Verify no Spring Boot dependencies remain in build.gradle
- Run `./gradlew dependencies` to check resolved Quarkus extensions
- Confirm no version conflicts in dependency tree
- Verify Quarkus extensions resolve correctly