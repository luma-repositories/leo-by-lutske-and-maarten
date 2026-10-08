# Title
Remove ORM and obsolete business logic after the JDBC switch

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — preserve stored data and workflows during architectural replacement; eventual story location.

## Objective
Finish the JDBC-only backend persistence boundary after production routes and all test fixtures have migrated.

## Scope
Remove Panache/ORM dependencies, entities/repositories, obsolete ORM configuration, and now-unused parser/converter-wrapper code and wrapper-only tests.

## Out of Scope
Schema/data migrations, Flyway history edits, database resets, destructive DB operations, AI delegate/model removal, public JSON changes, architecture suite (0036), and workflow suite (0037).

## Clean Architecture Placement
`:backend` retains outer JDBC/AI/REST/CDI adapters. `:core:domain` and `:core:usecases` remain pure Java with inward Gradle dependencies. ORM may survive only in untouched paths before this task; no new ORM functionality is authorized.

## Execution Dependencies
- `0024-preserve_recipe_browsing_and_import_workflows-convert_tests_to_jdbc_fixtures.md`

## Implementation Details
- Confirm all three resources use core use cases, JDBC read detail is complete, JDBC save owns commit, and all test sources compile without entity/repository usage. Then delete the two entity files and two old repository files listed below; do not delete JDBC adapters with similar names.
- Remove `implementation(libs["quarkusHibernateOrm"]!!)` from backend build and the `quarkusHibernateOrm: io.quarkus:quarkus-hibernate-orm-panache` catalogue entry from platform. Inspect resolved dependency graph for remaining ORM/Panache; remove only actual obsolete direct declarations rather than unrelated extensions.
- Retain PostgreSQL JDBC, Quarkus Flyway, Arc/REST/Jackson, LangChain4j, and test-scoped `quarkusTestH2` (which is `io.quarkus:quarkus-jdbc-h2`, not an ORM test library). Keep H2 PostgreSQL-mode datasource and Flyway initialization for ordinary tests. Remove obsolete `quarkus.hibernate-orm.database.generation=none` in main/test properties. Keep JDBC transaction capabilities; do not add core/JTA annotations to compensate.
- After searching actual references, remove unused `RecipeParserService` and temporary outer `IngredientConverter` wrapper; remove wrapper-only backend `IngredientConverterTest` once its algorithm characterization is retained in core task 0005. Do not remove the domain converter or its tests. If another genuine caller remains, complete its already-planned switch before removal rather than force-delete a needed class.
- **Retain required outer AI types:** `LangChain4jRecipeExtractionService`, `ChatModelProducer`, `RecipeExtractionService`, legacy `ExtractionResult`, legacy `RecipeExtractionException`, and their meaningful tests. Task 0018 deliberately delegates through those types to avoid the incompatible-return-type dual-interface conflict.
- **Retain used wire compatibility type:** `application.service.IngredientConversion` is still referenced by `RecipeImportDtos` and explicitly mapped by `RecipeImportDtoMapper`; it is not unused business logic. Avoid moving/deleting it or exposing domain records in DTOs as an incidental cleanup.
- Retain V1, V2, V3, and V4 byte-for-byte, including existing historical identity restarts in V3 and metadata TEXT in V4. Add no V5, reseeding, startup repair, sequence manipulation, ALTER, TRUNCATE, DROP, or Flyway clean. Replacing ORM changes access code only, not existing rows or schema. Do not manipulate generated `bin`/`build` artifacts as if they were source architecture.

## Files / Modules Impacted
- `backend/build.gradle.kts`
- `platform/quarkus-platform.gradle`
- `backend/src/main/resources/application.properties`
- `backend/src/test/resources/application.properties`
- Delete after checks: `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/entity/CategoryEntity.java`
- Delete after checks: `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/entity/RecipeEntity.java`
- Delete after checks: `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/repository/CategoryRepository.java`
- Delete after checks: `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/repository/RecipeRepository.java`
- Remove unused: `backend/src/main/java/be/lutske/leolegacy/application/service/RecipeParserService.java`
- Remove unused wrapper: `backend/src/main/java/be/lutske/leolegacy/application/service/IngredientConverter.java`
- Remove wrapper-only tests: `backend/src/test/java/be/lutske/leolegacy/application/service/IngredientConverterTest.java`
- Preserve unchanged: `backend/src/main/resources/db/migration/V1__init.sql`, `backend/src/main/resources/db/migration/V2__seed.sql`, `backend/src/main/resources/db/migration/V3__add_recipe_import_fields.sql`, `backend/src/main/resources/db/migration/V4__add_import_metadata.sql`.

## Acceptance Criteria
- Given the migrated codebase, when compiling production and all test sources, then no entity/Panache/EntityManager references remain and JDBC adapters supply the ports.
- Given ordinary tests, when run with H2/Flyway and deterministic AI mocks, then browsing/import adapter contracts pass without ORM.
- Given V1–V4 and existing stored data, when reviewing the change, then no migration content or database transformation was introduced and AI delegation still compiles.

## Testing Requirements
Run `./gradlew :core:domain:test :core:usecases:test :backend:compileTestJava :backend:test -x :backend:buildFrontend -x :backend:copyFrontend` (ordinary test retains integration exclusion). Inspect `./gradlew :backend:dependencies --configuration runtimeClasspath` and `./gradlew :backend:dependencies --configuration testRuntimeClasspath` for absence of Hibernate ORM/Panache and presence of JDBC/Flyway/H2 as appropriate. Compare migration file hashes/diff against the pre-change baseline. Search source/test imports for ORM leftovers and ensure existing delegate tests still run. These are removal checks, not a new architecture-test task. Do not start the legacy PostgreSQL profile or compose DB.

## Dependencies / Preconditions
JDK 25 and dependency resolution access; no external DB or live AI credentials.
