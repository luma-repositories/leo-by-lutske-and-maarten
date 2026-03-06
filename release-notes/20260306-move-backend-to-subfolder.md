## Summary
- Moved all backend code (Kotlin sources, resources, static site, tests) from the repository root `src/` into a `backend/` subdirectory.
- Converted the project to a Gradle multi-module layout with `backend` as a subproject.
- Root `build.gradle.kts` is now a thin file; all Quarkus build logic lives in `backend/build.gradle.kts`.
- `platform/quarkus-platform.gradle` remains at the root level, shared across modules.

## User impact
- Build commands now target the backend subproject: `./gradlew :backend:build`, `./gradlew :backend:quarkusDev`, `./gradlew :backend:test`.
- The packaged JAR is now at `backend/build/quarkus-app/quarkus-run.jar`.
- No changes to runtime behavior — the website and API work identically.

## Config/env changes
- None. `application.properties` moved with the rest of `src/` into `backend/src/main/resources/`.

## Platform dependency notes
- No dependency changes. `platform/quarkus-platform.gradle` stays at root; `backend/build.gradle.kts` references it via `rootProject.projectDir`.

## Migration notes
- `src/` → `backend/src/`
- `build.gradle.kts` (root) → thinned out; Quarkus config moved to `backend/build.gradle.kts`
- `settings.gradle.kts` updated with `include("backend")`

## How to verify
1. Run `./gradlew :backend:build` — build should succeed.
2. Run `./gradlew :backend:test` — all 6 tests should pass.
3. Run `./gradlew :backend:quarkusDev` — site at `http://localhost:8080/`, API at `http://localhost:8080/api/version`.
