# Title
Delegate reviewed confirmation with committed-save-only HTTP 201

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — confirmation, default category, and failure recovery; eventual story location.

## Objective
Replace confirmation's entity construction/business logic with the pure use case and atomic JDBC write adapter.

## Scope
Confirmation request/output DTO mapping, use-case/Clock producers, validation/server-error mapping, and removal of direct ORM from RecipeImportResource.

## Out of Scope
Core merge rule changes, UI behavior, persistence retry/deduplication, new category creation, schema changes, legacy class deletion (0025), and cross-layer import workflow suite (0037).

## Clean Architecture Placement
REST owns HTTP/DTOs; configuration constructs the pure use case with core ports, domain converter, and JDK Clock. JDBC owns transaction completion. Core stays pure Java.

## Execution Dependencies
- `0012-preserve_recipe_browsing_and_import_workflows-confirm_recipe_import_use_case.md`
- `0017-preserve_recipe_browsing_and_import_workflows-jdbc_recipe_write_repository.md`
- `0022-preserve_recipe_browsing_and_import_workflows-wire_recipe_extraction_endpoint.md`

## Implementation Details
- Inject `ConfirmRecipeImportUseCase`; `POST /api/recipes/import/confirm` still accepts JSON `ImportConfirmRequest`. Null request or null proposal is an explanatory 400 (`"Proposed recipe is required"`) before core invocation. Invalid JSON remains framework 400. Do not normalize blanks/empty lists into nulls.
- Extend the separate `RecipeImportDtoMapper` with `toConfirmCommand(ImportConfirmRequest)` and `toDetailResponse(RecipeDetail)`. Map rawModelResponse exactly; proposal's ten fields exactly; nullable overrides' seven fields title/ingredients/preparation/categoryId/notes/servings/description exactly. Convert old outer conversion records back into the six-field domain records explicitly, retaining null list versus empty. Domain handles ignoring servings/description overrides; mapper transports them. Map saved detail's seven fields to existing `RecipeDetailResponse`.
- Call `execute(command)` once. Map domain `RecipeImportValidationException` to HTTP 400 with its exact Title/Ingredients/Preparation required message. Map `ImportCategoryNotFoundException` to 500 error with `Default import category not found`; SQL/category lookup/save runtime failures return 500, never 400/201 or a synthetic detail. Use an explanatory JSON `error` body for handled errors; do not leak raw SQL or credentials. Preserve cause in server-side failure handling.
- Construct 201 solely from `output.recipe()` after save returns. Remove method `@Transactional`: the write adapter commits its local JDBC transaction before return. No outer interceptor may defer commit until after response construction. Do not re-fetch or fabricate a saved ID/detail.
- Remove old RecipeRepository/CategoryRepository/entity/extraction-service/converter-wrapper dependencies from this resource once both routes use core. Delete its now-redundant override, entity construction, entity detail mapping, and conversion helpers. Preserve validator/file-read handling and route annotations.
- Extend the existing producer with `Clock` from `Clock.systemUTC()` and `new ConfirmRecipeImportUseCase(coreCategoryRepository, coreRecipeWriteRepository, domainIngredientConverter, clock)`. Reuse 0022's domain converter producer. No producer for the static `ResolveReviewedRecipe` helper and no second adapter bean. Default UI category resolves to 15; API category overrides/fallback remain exactly task 0012.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeImportDtoMapper.java`
- `backend/src/main/java/be/lutske/leolegacy/configuration/RecipeUseCaseProducer.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeImportDtoMapperTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResourceTest.java`

## Acceptance Criteria
- Given valid reviewed input, when confirmation returns 201, then its generated detail is already committed and visible on another JDBC connection.
- Given absent request/proposal or invalid required fields, when confirming, then 400 occurs without writes.
- Given missing default category or failed save/commit, when confirming, then 500 occurs and no successful-save payload is returned.

## Testing Requirements
Mapper tests cover complete/null overrides, nullable raw text, null versus empty ingredients/preparation, category IDs, notes, String servings, and all conversion fields. H2 resource tests cover valid 201 plus direct SQL lookup by returned ID, 400 validation order, null request/proposal, and 500 mapping using controlled failing core-port adapters. Use test-scoped CDI replacement of the category/write port when needed rather than removing seeded category 15 from a shared test DB; real rollback is tested in 0017/0035. Assert no 201 on forced save failure and no persistence on validation. Keep fixtures JDBC-only. Run `./gradlew :backend:test --tests be.lutske.leolegacy.interfaceadapter.rest.mapper.RecipeImportDtoMapperTest --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeImportResourceTest -x :backend:buildFrontend -x :backend:copyFrontend` and `./gradlew :core:domain:test :core:usecases:test :backend:compileTestJava -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing H2 test datasource; category 15 must exist in deployed storage for UI-default imports, with failure rather than repair if absent. No live AI required.
