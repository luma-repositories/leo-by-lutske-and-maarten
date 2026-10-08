# Title
Route all recipe browsing through three core use cases

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — popular, category, and detail browsing; eventual story location.

## Objective
Remove direct ORM access from RecipeResource while retaining its API and read-only behavior.

## Scope
Wire ListRecipesUseCase, GetPopularRecipesUseCase, and GetRecipeUseCase; extend the existing focused producer.

## Out of Scope
Import routes, additional list ordering, view increments, changing DTO contracts, ORM removal before other paths migrate, and schema changes.

## Clean Architecture Placement
Outer REST performs DTO/HTTP mapping; outer configuration constructs pure core use cases over the completed JDBC read adapter.

## Execution Dependencies
- `0007-preserve_recipe_browsing_and_import_workflows-list_recipes_use_case.md`
- `0008-preserve_recipe_browsing_and_import_workflows-get_popular_recipes_use_case.md`
- `0009-preserve_recipe_browsing_and_import_workflows-get_recipe_use_case.md`
- `0016-preserve_recipe_browsing_and_import_workflows-jdbc_recipe_detail_query.md`
- `0019-preserve_recipe_browsing_and_import_workflows-wire_category_resource.md`

## Implementation Details
- Constructor-inject all three use cases. Preserve `GET /api/recipes?categoryId=...`: pass nullable `Long` to `ListRecipesUseCase.execute(Long)` exactly; null means all, not top ten. Unknown/zero/negative category IDs retain empty-query behavior.
- Preserve `GET /api/recipes/top`: `GetPopularRecipesUseCase.execute()` requests ten through the port. No REST sorting/limit.
- Preserve `GET /api/recipes/{id}`: pass long unchanged to `GetRecipeUseCase.execute(long)`; map empty Optional to `NotFoundException("Recipe with id " + id + " not found")`. Other failures are server errors, not 404.
- Explicitly map summary fields id/title/categoryName/viewCount to `RecipeSummaryResponse`; map detail id/title/ingredients/preparation/categoryId/categoryName/viewCount to `RecipeDetailResponse`. Decoding now belongs to JDBC; do not split/reconvert in REST. Retain integer zero view counts in JSON and exact text/order.
- Extend `RecipeUseCaseProducer` with three `@Produces` methods using core `RecipeReadRepository` constructor parameters. Reuse one `@ApplicationScoped JdbcRecipeReadRepository` bean. Keep core unannotated and producer-returned use cases dependent-scoped. Verify the 0015 placeholder is gone before wiring.
- Delete this resource's old entity/repository imports and mapping helpers tied to entities; ordinary domain-to-DTO helpers may remain private.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeResource.java`
- `backend/src/main/java/be/lutske/leolegacy/configuration/RecipeUseCaseProducer.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeResourceTest.java`

## Acceptance Criteria
- Given all/category/top requests, when called, then the correct use case supplies the unchanged summary shape and the popular result is descending with at most ten rows.
- Given an existing detail read twice, when comparing raw storage, then no view count or other value changes.
- Given a missing recipe, when GET is called, then 404 is retained; storage failure is not misreported as absence.

## Testing Requirements
Add controlled failing core `RecipeReadRepository` port REST tests behind the real use cases/resource for all/category list, top, and detail requests. Assert HTTP 500 for each failure, never 200 with an empty list and never 404 for a failed detail lookup; retain a separate genuine-missing-detail 404 test. Scope replacements to the test/profile and restore/reset in finally or teardown even after assertion failure. Success tests must still use JDBC and pass independently of ordering; do not break the shared datasource to simulate failure.

Extend existing H2 REST tests with SQL-baseline assertions for all/category/top sets, zero views, detail whitespace/order/category ID, missing category/recipe, and repeated read immutability. Use JDBC-only test-owned rows for high-count ranking fixtures (above seed counts); clean them by generated ID. Keep SQL fixture code local until 0024. Run `./gradlew :backend:test --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeResourceTest --tests be.lutske.leolegacy.interfaceadapter.rest.CategoryResourceTest -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing H2/Flyway test configuration; no external DB.
