# Title
Create the Java 25 core Gradle modules

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — Preserve recipe browsing and import workflows. This is the eventual story location; this task does not move it.

## Objective
Establish compile-time boundaries for pure domain and use-case code while keeping the existing backend buildable.

## Scope
Register `:core:domain` and `:core:usecases`; configure Java 25 and isolated JUnit testing; add the backend's inward dependencies.

## Out of Scope
Application migration, REST rewiring, CDI producers, JDBC adapters, ORM additions, schema changes, UI changes, and moving the story.

## Clean Architecture Placement
Follow [the architecture reference](https://github.com/maarten-vandeperre/clean-architecture-software-sample-project): `:backend` is the outer Quarkus composition module; `:core:usecases` depends on `:core:domain`; domain depends only on the JDK. Compile-time Gradle modules, not merely packages, enforce this direction. No production framework, logging, JSON, persistence, or AI dependencies in either core module. No JPMS requirement.

## Execution Dependencies
None.

## Implementation Details
- Add `include(":core:domain", ":core:usecases")` alongside the existing backend include in `settings.gradle.kts`. Default project directories are `core/domain` and `core/usecases`.
- Both new build files apply `java-library`, use Maven Central, group `be.lutske`, version `1.0.0-SNAPSHOT`, Java toolchain `JavaLanguageVersion.of(25)`, UTF-8 compilation, and `-parameters`.
- Domain has no production dependency declarations. Usecases declares `api(project(":core:domain"))`, because its public contracts expose domain types.
- Backend adds `implementation(project(":core:domain"))` and `implementation(project(":core:usecases"))`. Keep the existing backend-only Quarkus plugin/platform configuration there; do not apply `platform/quarkus-platform.gradle` to core.
- In each core module use test-only `testImplementation(platform("org.junit:junit-bom:5.11.4"))`, `testImplementation("org.junit.jupiter:junit-jupiter")`, `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`, and `tasks.withType<Test>().configureEach { useJUnitPlatform() }`. Do not inherit backend's JBoss logging manager setting or Quarkus test setup.
- Reserve packages `be.lutske.leolegacy.domain` and `be.lutske.leolegacy.usecases`; later tasks put public top-level types directly in these packages. No production placeholders are needed.

## Files / Modules Impacted
- `settings.gradle.kts`
- `backend/build.gradle.kts`
- `core/domain/build.gradle.kts`
- `core/usecases/build.gradle.kts`
- `core/domain/src/test/java/be/lutske/leolegacy/domain/CoreModuleSmokeTest.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/CoreModuleSmokeTest.java`

## Acceptance Criteria
- Given the root build, when Gradle lists projects, then both exact project IDs appear at the specified directories.
- Given either core module, when its tests run, then ordinary JUnit executes on Java 25 without Quarkus or backend classes.
- Given the backend, when it compiles, then its existing code still compiles with the new inward dependencies.

## Testing Requirements
Add one ordinary JUnit smoke test per module asserting `Runtime.version().feature() == 25`; these establish test discovery/toolchain wiring before business fixtures exist. From the repository root run `./gradlew projects :core:domain:test :core:usecases:test :backend:compileJava`. Inspect `./gradlew :core:domain:dependencies --configuration runtimeClasspath` and `./gradlew :core:usecases:dependencies --configuration runtimeClasspath`: domain has no external runtime dependencies; usecases has only domain. No Quarkus tests in core.

## Dependencies / Preconditions
JDK 25 and access to the existing Gradle distribution and Maven Central.
