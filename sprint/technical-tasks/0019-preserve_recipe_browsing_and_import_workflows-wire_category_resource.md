# Title
Route CategoryResource through ListCategoriesUseCase

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — alphabetical category/count browsing; eventual story location.

## Objective
Replace the category endpoint's direct ORM and N+1 recipe loading with the pure use case and JDBC aggregate adapter.

## Scope
Change CategoryResource constructor/delegation and introduce a focused `RecipeUseCaseProducer` containing only the category use-case producer initially.

## Out of Scope
Recipe/import endpoint rewiring, generic dependency registries, removing still-used ORM classes, new ORM functionality, and database/schema changes.

## Clean Architecture Placement
REST mapping and CDI composition are outer `:backend`; `ListCategoriesUseCase` remains a plain core class. JDBC implements the inward repository port.

## Execution Dependencies
- `0006-preserve_recipe_browsing_and_import_workflows-list_categories_use_case.md`
- `0014-preserve_recipe_browsing_and_import_workflows-jdbc_category_repository.md`

## Implementation Details
- Constructor-inject core `ListCategoriesUseCase` into `CategoryResource`. `GET /api/categories` remains JSON HTTP 200; call `execute()` once and explicitly map each `CategoryWithCount(id, name, recipeCount)` to existing `CategoryResponse(long id, String name, long recipeCount)` in returned order.
- Remove both old repository imports/fields from this resource. No Java sorting, per-category recipe query, count narrowing, or fallback empty response on repository errors.
- Create `be.lutske.leolegacy.configuration.RecipeUseCaseProducer` as an outer CDI bean. Add an `@Produces` method returning `new ListCategoriesUseCase(categoryRepository)` using the **core** `CategoryRepository` parameter; its sole implementation is `JdbcCategoryRepository`. Default dependent producer scope is sufficient for plain core objects and avoids adding proxy requirements/annotations to core.
- Do not also produce repository instances already supplied by `@ApplicationScoped` adapters. Use qualified imports/full names where old/core repository simple names collide. SQL exceptions remain server errors, not successful empty sidebar data.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java`
- `backend/src/main/java/be/lutske/leolegacy/configuration/RecipeUseCaseProducer.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/CategoryResourceTest.java`

## Acceptance Criteria
- Given seeded plus empty categories, when GET is called, then HTTP shape/order/counts are preserved, including zero.
- Given CDI startup, when resolving the category use case, then the JDBC core-port implementation is injected without ambiguity.
- Given a repository failure, when listing, then the endpoint returns HTTP 500, never HTTP 200 with an empty list.

## Testing Requirements
Add a REST request test with a controlled failing core `CategoryRepository` port behind the real use case/resource. Assert `GET /api/categories` returns exactly 500, not 200 with empty data. Scope the CDI replacement to that test/profile and restore/reset it in finally or teardown, including assertion failure; ordinary success tests must still use JDBC and pass regardless of execution order. Do not simulate failure by damaging the shared database.

Extend `CategoryResourceTest` using injected `DataSource` and bound JDBC for any new fixtures/assertions; no new ORM fixtures. Compare counts and ordering with direct aggregate SQL, including one test-owned empty category, and clean up only that category's generated ID. Existing seed-only tests remain valid. Run `./gradlew :backend:test --tests be.lutske.leolegacy.interfaceadapter.rest.CategoryResourceTest -x :backend:buildFrontend -x :backend:copyFrontend`. This verifies CDI and REST against the existing H2 profile; never run the legacy PostgreSQL integration profile here.

## Dependencies / Preconditions
Existing H2 test datasource; no external database.
