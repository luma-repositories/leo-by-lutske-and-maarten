# Title
Convert persistence-dependent test setup and assertions to owned JDBC fixtures

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — preserve behavior while replacing persistence technology; eventual story location.

## Objective
Make all test sources compile and ordinary tests pass without entity/Panache dependencies before removing ORM.

## Scope
Introduce an exact shared JDBC fixture helper; migrate any remaining entity/repository setup/assertions, including tagged test sources, and consolidate temporary inline JDBC fixtures from earlier slices.

## Out of Scope
Production behavior changes, adding ORM fixtures, deleting seeds, global database cleanup, live PostgreSQL integration execution, architecture suite (0036), and cross-layer workflow suite (0037).

## Clean Architecture Placement
Test support only in outer `:backend`; tests use injected `DataSource` or private H2 datasources and core/HTTP outputs. No JDBC fixtures or Quarkus testing in production core.

## Execution Dependencies
- `0020-preserve_recipe_browsing_and_import_workflows-wire_recipe_browsing_resource.md`
- `0023-preserve_recipe_browsing_and_import_workflows-wire_recipe_confirmation_endpoint.md`

## Implementation Details
- Add `backend/src/test/java/be/lutske/leolegacy/testsupport/JdbcRecipeFixtures.java` as a plain helper constructed with `DataSource`. Offer generated-ID category/recipe creation, raw recipe snapshots, parameterized count/lookups, explicit registration of recipe IDs returned by confirmation, and `AutoCloseable` cleanup. Return test records/JDK values, never entities or production repositories.
- Use `INSERT INTO category (name) VALUES (?)` and the exact recipe INSERT from 0017; bind strings, long category IDs, integer view counts, nullable source/metadata, and UTC TIMESTAMP explicitly. Capture generated keys and track ownership per helper/test. Never assume IDs start at 1/1000, allocate `MAX(id)+1`, or reset identities. V3's existing sequence restarts are migration history, not fixture instructions.
- Cleanup with `DELETE FROM recipe WHERE id = ?` only for explicitly recorded owned recipe IDs, then `DELETE FROM category WHERE id = ?` only for owned category IDs. Execute cleanup transactionally with rollback/resource closure. No `deleteAll`, unqualified DELETE, TRUNCATE, DROP, Flyway clean, seed deletion, or deletion by broad title/category pattern. Never register seed category 15 as owned. If insertion fails before ID capture, transaction rollback must remove that attempt.
- Assert imported rows by IDs returned from responses, not mutable entity state or assumptions about unrelated counts. Use count deltas/raw snapshots for no-write checks. Existing CategoryResourceTest/RecipeResourceTest currently use seed-only HTTP assertions; RecipeImportResourceTest uses a LangChain4j mock and leaves confirmed rows behind; RecipeImportIntegrationTest uses HTTP and is tagged. Do not invent existing ORM fixture usage: audit the actual sources after earlier tasks and replace only remaining usages, plus add ownership cleanup where missing.
- Capture the raw confirmation response first, extract and register the new recipe ID immediately, then perform substantive status/body/content/SQL assertions. Do not chain assertions before ownership registration. Enclose fixture lifetime in try-with-resources/finally or unconditional `@AfterEach` cleanup so assertion failure still removes recorded rows. If a committed HTTP write cannot be identified from its response, that scenario may run only in a disposable test-owned isolated database whose lifecycle is destroyed afterward; never infer ownership through broad deletes or remove shared rows to recover.
- Check **all** test source compilation, not just executed tests: Gradle compiles the legacy tagged integration class even though ordinary `test` excludes `integration`. Replace any entity/repository imports introduced or retained there, use long recipe IDs, and retain its compileability. Do not run its current localhost/clean-at-start profile; quarantine/redirect is explicitly task 0026.
- Preserve old AI delegate mocks/models; they are not ORM. Consolidate adapter/resource fixture code where helpful without turning the helper into a generic production data-access layer.

## Files / Modules Impacted
- `backend/src/test/java/be/lutske/leolegacy/testsupport/JdbcRecipeFixtures.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/CategoryResourceTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeResourceTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResourceTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportIntegrationTest.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcCategoryRepositoryTest.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeReadRepositoryTest.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeWriteRepositoryTest.java`

## Acceptance Criteria
- Given all test sources, when compiled, then no setup/assertion requires an ORM entity or Panache repository, including excluded tagged sources.
- Given a test creates categories/recipes or confirms an import, when it finishes, then only its tracked rows are deleted and seed/unrelated rows remain byte-for-byte intact.
- Given no-write/error tests, when assertions run, then JDBC snapshots/count deltas prove the expected result independently of ORM caches.

## Testing Requirements
Add a deterministic cleanup test that performs a real successful confirmation, captures/registers its ID, deliberately throws an assertion failure inside the protected fixture scope, and catches/asserts that expected failure outside it. Verify afterward from a separate connection that the owned row is gone and seed/unrelated snapshots are unchanged. Cover owned category cleanup too; a failed-insert test alone does not prove assertion-failure cleanup.

Run `./gradlew :backend:compileTestJava -x :backend:buildFrontend -x :backend:copyFrontend`, then `./gradlew :backend:test --tests 'be.lutske.leolegacy.infrastructure.persistence.jdbc.*Test' --tests be.lutske.leolegacy.interfaceadapter.rest.CategoryResourceTest --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeResourceTest --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeImportResourceTest -x :backend:buildFrontend -x :backend:copyFrontend`. Verify a before/after snapshot of seed rows around fixture cleanup and test cleanup after a failing insert. Search `backend/src/test` for persistence.entity, persistence.repository, Panache, EntityManager, and ORM test transactions and resolve actual dependencies. Do not execute integration-tagged tests yet.

## Dependencies / Preconditions
Existing H2 JDBC/Flyway test profile; no external database or AI credentials.
