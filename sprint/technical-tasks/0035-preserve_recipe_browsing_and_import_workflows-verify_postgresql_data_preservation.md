# Title
Verify JDBC data preservation on disposable PostgreSQL 17

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — historical data preservation and committed import persistence; eventual story location.

## Objective
Prove the JDBC adapters preserve existing PostgreSQL schema/rows and enforce committed-save/rollback behavior using the disposable harness from 0026.

## Scope
Direct JDBC adapter and confirmation-use-case preservation assertions, owned historical fixtures, and extension of the existing explicit runner to select the preservation class.

## Out of Scope
Harness recreation, a second pre-V3 database, HTTP upload/review/navigation workflows (0037), architecture enforcement (0036), live AI, migration changes, DB repair, performance benchmarking, and production schema/data operations.

## Clean Architecture Placement
Outer backend integration tests exercise real JDBC repositories and the pure confirmation use case directly. Testcontainers/Quarkus stay test-scoped; production core receives no testing dependencies.

## Execution Dependencies
- `0026-preserve_recipe_browsing_and_import_workflows-postgresql_jdbc_preservation_integration.md`
- Adapter, fixture, and confirmation prerequisites are already transitive through 0026 → 0025 → 0024; no frontend or workflow-suite dependency is required.

## Implementation Details
- Add `PostgresJdbcPreservationTest` with `@QuarkusTest`, `@TestProfile(PostgresIntegrationTestProfile.class)`, and `@Tag("integration")`. Reuse the fresh non-reusable PG17 resource and deterministic AI alternative from 0026. Call adapters/use cases directly, not REST/React.
- Extend `jdbcPreservationTest`'s include filter to include `be.lutske.leolegacy.infrastructure.persistence.jdbc.PostgresJdbcPreservationTest` **and retain** `PostgresJdbcBootstrapTest`. Retain integration-tag selection, explicit legacy-class exclusion, dedicated reports, and failure when no selected tests run. Ordinary `test` still skips integration but compiles all sources.
- Initialize the fresh test-owned database with **unchanged V1–V4** through the existing Flyway profile once; never clean/rebaseline or rerun V3's historical sequence restart on an existing DB. Add JDBC-owned historical fixture rows before taking the preservation baseline: pipe/blank/whitespace ingredients, multiline preparation, nonzero/zero views, nullable/empty source/metadata, UTC created_at, and categories including an empty one. Use generated keys and helper ownership tracking, with unconditional cleanup on assertion failure.
- Snapshot all baseline `category(id,name)` rows and all `recipe(id,title,ingredients,preparation,category_id,view_count,source,created_at,import_metadata)` rows by ID, including original seed rows. Capture schema metadata from `information_schema.columns`, primary/foreign/unique constraints, `pg_indexes` (including `idx_recipe_category`), identity definitions, and Flyway history version/checksum/success. Canonicalize only metadata ordering, not recipe text. Database-generated timestamp precision is the comparison baseline.
- Exercise JDBC all/category/top/detail/category-count/lookup methods. Compare unordered all/category sets and exact detail strings/category IDs with independent prepared SQL; compare top ten descending counts without inventing a tie-breaker, including >10 distinct high-count owned rows. Verify empty category counts, unknown IDs, and repeated detail reads leave all raw baseline rows unchanged.
- Invoke real `JdbcRecipeWriteRepository` with a fixed `NewRecipe` and separately real `ConfirmRecipeImportUseCase` with JDBC ports/domain converter/fixed UTC Clock. Register returned IDs before substantive assertions. Verify generated IDs are unique, absent from baseline, and visible from a **different** connection after return; do not hardcode 1000 or require gap-free identities. Assert exact inserted title/pipe ingredients/preparation-with-notes/category 15/zero views/source/UTC timestamp/raw metadata, conversion display idempotence, and returned detail. Existing baseline rows remain identical; only owned inserts are additional. Recompute category counts and read parity after writes.
- Validate bad title/empty ingredients/blank preparation via the real pure confirmation use case over JDBC ports: failure precedes save, no row/count changes. This is persistence no-write verification, not an HTTP flow matrix.
- The missing-default-category rule is already covered by core and REST tests; it does not require an additional pre-V3 database. If retained as a local no-write assertion, use the same migrated test-owned PG17 database and separately construct the pure use case with a category-port double reporting absence plus a spy delegating to the real JDBC write adapter. Assert `ImportCategoryNotFoundException`, zero save invocations, unchanged rows/counts, and no category creation for null category/missing requested ID fallback. This proves a port rule, not real PostgreSQL category absence. Never delete seed category 15 or alter migrations for this scenario.
- Test FK and title/source-length constraint failure plus a delegating DataSource/Connection that executes a real INSERT then throws before actual commit. Assert rollback using a separate connection, no returned detail, unchanged baseline rows, and released resources. Include post-insert detail/key failure on PostgreSQL in the same harness. Keep 0017's acknowledged-commit/cleanup contract; a lost commit acknowledgement cannot guarantee rollback. Test controlled pre-commit failures and do not claim distributed exactly-once behavior.
- Compare baseline schema definitions and Flyway history after reads/writes/failures. Identity **values** may advance during successful or rolled-back inserts; definitions must not change and historical IDs remain stable. No sequence reset assertions or fixes. Cleanup only helper-owned inserted IDs, recipe before category; destroy the disposable container at suite end. Never issue DROP/TRUNCATE/Flyway clean against a database.

## Files / Modules Impacted
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/PostgresJdbcPreservationTest.java`
- `backend/src/test/java/be/lutske/leolegacy/testsupport/JdbcRecipeFixtures.java` only for necessary preservation snapshots/owned fixtures.
- `backend/build.gradle.kts` only to extend the existing runner's include filter.
- Reuse unchanged: `PostgresJdbcTestResource`, `PostgresIntegrationTestProfile`, `StubRecipeExtractionRepository`, and `PostgresJdbcBootstrapTest` from 0026.

## Acceptance Criteria
- Given the disposable migrated PG17 database, when JDBC reads, successful saves, and controlled failures complete, then baseline rows/schema/history are identical, committed generated-ID inserts are visible on another connection, and failed/invalid operations add no rows.
- Given historical ingredient/text/null/time fixtures, when adapters read those fixtures, then SQL baselines and adapter results preserve the detailed comparisons above without text normalization or identity assumptions.
- Given the explicit runner, when it executes, then both bootstrap and preservation classes execute with nonzero tests, while ordinary tests exclude integration and the legacy live-AI flow remains disabled.
- Given success or assertion failure, when teardown runs, then only owned fixtures are deleted and the disposable resource is stopped.

## Testing Requirements
Run `./gradlew :backend:compileTestJava -x :backend:buildFrontend -x :backend:copyFrontend`, then `./gradlew :backend:jdbcPreservationTest -x :backend:buildFrontend -x :backend:copyFrontend`. Inspect the dedicated report for nonzero tests in **both** `PostgresJdbcBootstrapTest` and `PostgresJdbcPreservationTest`. Record schema/row comparisons and generated-ID/rollback outcomes in assertions, not logs alone. Run ordinary `./gradlew :backend:test -x :backend:buildFrontend -x :backend:copyFrontend` to verify isolation/tag exclusion. These focused persistence checks do not replace the later production-build gate: after the frontend tasks, run the final build without frontend exclusions; 0034 separately requires full `npm run test` and `npm run build`.

## Dependencies / Preconditions
Completed 0026 harness, JDK 25, dependency/image download access, and Docker or compatible Podman Testcontainers runtime capable of launching `postgres:17`. No existing PostgreSQL service, mounted data, API key, or live AI provider is required.
