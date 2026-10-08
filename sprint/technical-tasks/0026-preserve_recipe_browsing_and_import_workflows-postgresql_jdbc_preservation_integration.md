# Title
Bootstrap the disposable PostgreSQL 17 JDBC test runner

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — historical data preservation and committed import persistence; eventual story location.

## Objective
Provide a fail-closed disposable PostgreSQL 17 test harness and prove Quarkus/Flyway bootstrap through the explicit runner.

## Scope
A bootstrap smoke test, deterministic AI substitute, disposable Testcontainers resource/profile, explicit Gradle integration task, and quarantine/redirection of the unsafe legacy PostgreSQL test path.

## Out of Scope
Persistence fixture/read/write/rollback/schema-preservation assertions (0035), HTTP upload→review→confirmation/navigation scenarios (0037), architecture enforcement suite (0036), live AI, compose/local database access, migration changes, DB repair, performance benchmarking, and production schema/data operations.

## Clean Architecture Placement
Quarkus test infrastructure, the smoke test, and Testcontainers remain test-scoped outer dependencies. Production core receives no testing/framework dependencies.

## Execution Dependencies
- `0025-preserve_recipe_browsing_and_import_workflows-remove_orm_and_unused_business_logic.md`

## Implementation Details
### Disposable database and legacy profile containment
- Add `PostgresJdbcTestResource` implementing `QuarkusTestResourceLifecycleManager`. Start a **fresh non-reusable** Testcontainers `PostgreSQLContainer` using `postgres:17`, random mapped port, generated container-local database credentials, and no bind mounts/reused volumes. Return the container JDBC URL/username/password, PostgreSQL db-kind, `quarkus.flyway.migrate-at-start=true`, `quarkus.flyway.clean-at-start=false`, and devservices disabled. Stop it in lifecycle cleanup, including startup failure. Fail closed on unavailable runtime; never fall back to localhost, compose, environment database URLs, or existing databases.
- Redirect existing `PostgresIntegrationTestProfile` to register that test resource via `testResources()` and enable the test-only deterministic AI alternative via `getEnabledAlternatives()`. Remove its hard-coded `jdbc:postgresql://localhost:5432/leo_legacy`, credentials, `clean-at-start=true`, and obsolete Tesseract settings/comments. Profile datasource values must come only from the successfully started container resource. Ensure the resource's returned properties override normal H2/main datasource settings before application boot.
- Quarantine `RecipeImportIntegrationTest` with class-level JUnit `@Disabled` explaining that its old live-AI/cross-layer coverage is superseded by reserved task 0037, retain compilation, and explicitly exclude its class from the new runner. This task does not rewrite its flows. Redirecting the profile removes the destructive local-DB route even if someone selects the legacy class later; no enabled integration runner may start it against an existing database.
- Add test-only `StubRecipeExtractionRepository` (`@Alternative`, `@ApplicationScoped`) implementing the core port with a fixed complete domain result and stable raw text/warnings/provider/model. Enable only in this profile. The smoke test verifies this alternative is resolved and returns the fixed result without external model calls or credentials. Legacy flow stays disabled. Do not change production AI defaults.

### Explicit runner
- Add test-only Testcontainers PostgreSQL/JUnit support as needed through `platform/quarkus-platform.gradle` catalogue entries and `backend/build.gradle.kts`; keep versions centralized there (or use Quarkus BOM-managed versions when present). If PostgreSQL Flyway support is not already resolved through the retained extension, add test/runtime module support appropriate to the existing managed Flyway version; do not replace Flyway or alter migrations.
- Current `tasks.withType<Test>` globally excludes `integration`. Refactor so the logging manager setting applies to all Test tasks, while **only** `tasks.named<Test>("test")` excludes `integration`.
- Register `jdbcPreservationTest` of type `Test`, using `sourceSets["test"].output.classesDirs` and `.runtimeClasspath`, `dependsOn(testClasses)`, `useJUnitPlatform { includeTags("integration") }`, and initially `filter { includeTestsMatching("be.lutske.leolegacy.infrastructure.persistence.jdbc.PostgresJdbcBootstrapTest") }`. Task 0035 extends this filter to also include `PostgresJdbcPreservationTest`, retaining the smoke test. Do not inherit an integration exclusion, and exclude `RecipeImportIntegrationTest` explicitly. Use a separate report/result destination and fail if no selected tests run. Verify Quarkus test bootstrap on this explicit task using the existing test source set/configuration.
- New `PostgresJdbcBootstrapTest` is `@QuarkusTest`, `@TestProfile(PostgresIntegrationTestProfile.class)`, `@Tag("integration")`. Ordinary `test` skips it but compiles all sources.

### Bootstrap smoke assertions
- Let Flyway initialize the fresh owned database with **unchanged V1–V4** once. Assert through the injected datasource that the connected server is PostgreSQL 17 and its connection target matches the started resource, migrations 1–4 succeeded, and the expected category/recipe tables are queryable. Do not clean/rebaseline or rerun historical migrations on an existing database.
- Assert the deterministic extraction port alternative is active. Keep this a bootstrap/profile/runner smoke test; detailed fixture comparisons, save visibility, rollback, and same-schema assertions belong to 0035.

## Files / Modules Impacted
- `platform/quarkus-platform.gradle`
- `backend/build.gradle.kts`
- `backend/src/test/java/be/lutske/leolegacy/testsupport/PostgresJdbcTestResource.java`
- `backend/src/test/java/be/lutske/leolegacy/testsupport/StubRecipeExtractionRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/PostgresJdbcBootstrapTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/PostgresIntegrationTestProfile.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportIntegrationTest.java` (quarantine only; workflow replacement belongs to 0037).

## Acceptance Criteria
- Given no running compose/local database and no AI credentials, when the explicit task runs with a container runtime, then the tagged bootstrap test executes against fresh PG17 and passes with deterministic dependencies.
- Given the disposable database resource, when application startup completes, then V1–V4 have succeeded on the owned PG17 target and the deterministic AI alternative is active.
- Given ordinary test execution or legacy class selection, when discovering tests, then integration isolation cannot invoke clean-at-start against localhost and the legacy live flow remains quarantined.

## Testing Requirements
Run `./gradlew :backend:compileTestJava -x :backend:buildFrontend -x :backend:copyFrontend`, then `./gradlew :backend:jdbcPreservationTest -x :backend:buildFrontend -x :backend:copyFrontend`. Inspect the dedicated report to confirm `PostgresJdbcBootstrapTest` actually ran (nonzero tests) rather than being excluded by the former global tag setting. Run `./gradlew :backend:test -x :backend:buildFrontend -x :backend:copyFrontend` to prove ordinary H2 tests still work and skip integration. Verify resource cleanup and fail-closed startup by testing unavailable container runtime without any datasource fallback; never test safety by pointing at a user's DB. These focused backend checks intentionally exclude frontend packaging; the later final production-build gate must run without those exclusions after the frontend tasks (including 0034's full test/build gate) are complete.

## Dependencies / Preconditions
JDK 25, dependency/image download access, and Docker or compatible Podman Testcontainers runtime capable of launching `postgres:17`. No existing PostgreSQL service, local database, mounted data, Tesseract, API key, or live AI provider is required.
