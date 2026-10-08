# Title
Implement ConfirmRecipeImportUseCase with successful-save-only output

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC7–AC8, conversion, default Imported category, and failure recovery; eventual story path.

## Objective
Validate reviewed content, resolve the existing category fallback, convert display ingredients, and return a saved detail only after persistence succeeds.

## Scope
One pure orchestration use case with category/write ports, the domain converter/helper, and an injected JDK clock.

## Out of Scope
JDBC/ORM adapters, schema changes, REST/CDI wiring, HTTP transaction handling, UI selector or other UI changes, view increments, retry/deduplication features, and saving description/servings/tags as new columns.

## Clean Architecture Placement
Public plain `be.lutske.leolegacy.usecases.ConfirmRecipeImportUseCase` in `:core:usecases`. It uses the domain's static merge helper and models. Quarkus composition later supplies ports, a domain converter, and `Clock.systemUTC()`; core imports no framework or transaction annotations.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`
- `0005-preserve_recipe_browsing_and_import_workflows-move_ingredient_conversion_to_domain.md`
- `0011-preserve_recipe_browsing_and_import_workflows-resolve_reviewed_recipe.md`

## Implementation Details
Exact public API (`Clock` is `java.time.Clock`, converter is the domain type):

```java
public ConfirmRecipeImportUseCase(CategoryRepository categoryRepository,
    RecipeWriteRepository recipeWriteRepository,
    IngredientConverter ingredientConverter, Clock clock);
public ConfirmRecipeImportOutput execute(ConfirmRecipeImportCommand command);
```

Keep dependencies final. The static helper is not a fifth constructor dependency. Execution:
1. Resolve/validate using `ResolveReviewedRecipe.resolve(command.proposedRecipe(), command.userOverrides())`. Do no lookup/save on validation failure. Commands/proposals are non-null boundary inputs; overrides/raw response may be null.
2. Re-run conversion on the resolved ingredient strings, using the exact task-0005 algorithm. Persist ordered conversion display values, not `proposedRecipe.convertedIngredients` metadata; corrected ingredient text is authoritative. Already-converted display strings remain unchanged, so confirmation adds no second conversion. This is display-conversion idempotence, not duplicate-request persistence deduplication. A repeated successful confirmation is not given new deduplication behavior.
3. Requested category is the resolved nullable category ID or `15L`. Query `categoryRepository.findById(requestedId)`; if absent and requested ID differs from 15, query 15 once. If category 15 is absent, throw `ImportCategoryNotFoundException` with `"Default import category not found"`; never create a category or save into a fabricated one. If requested category exists, use it even if category 15 is missing. Skipping a redundant second lookup when requested ID is already 15 has the same fallback outcome.
4. Build `NewRecipe` with merged title, converted display ingredients, final preparation (notes already appended), resolved existing category's ID, `viewCount=0`, source equal to proposal source when non-null (including empty String), otherwise `"Imported from image"`; `createdAt=Instant.now(clock)`; `importMetadata=command.rawModelResponse()` unchanged, including null. Do not substitute "Untitled Recipe": required validation has already run.
5. Call `recipeWriteRepository.save(newRecipe)` once. Only after it returns its persisted `RecipeDetail`, construct `new ConfirmRecipeImportOutput(savedDetail)`. Return that exact saved detail rather than reconstructing an optimistic response or fetching it again. Propagate save/category repository failures; do not catch and emit a success output, mutate the input, retry internally, or clear review state.

Current UI does not select or send a category: its null proposal/override category flows to 15. Existing API clients can still specify proposed or override category IDs, with a non-null override taking precedence and missing requested categories falling back to 15. Keep this compatibility without adding a selector.

Later adapter handoff: encode the ordered ingredient list in the existing storage representation, preserve source/Instant/metadata fields, generate the recipe ID, and map the persisted detail. The save contract and outer transaction must ensure a failed insert/commit cannot produce HTTP success; REST will map validation/category/persistence failures without navigating or discarding review edits. Core returns data/exceptions, not navigation or HTTP 201. Do not add transaction annotations to this use case.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ConfirmRecipeImportUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/ConfirmRecipeImportUseCaseTest.java`

## Acceptance Criteria
- Given valid reviewed corrections and notes with no category selection, when confirming, then one recipe is saved under category 15 with converted ordered ingredients, notes, zero views, and the injected timestamp, and output carries its generated detail.
- Given an existing requested category, when confirming, then that category is used; given a missing requested category, then 15 is tried; given neither available, then confirmation fails without saving.
- Given a converted proposal, when confirming, then no second conversion is added.
- Given invalid required content or a save failure, when confirming, then no successful output is returned and input review data remains unchanged.

## Testing Requirements
Plain JUnit; recording category/write fakes, real domain converter and helper, `Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC)`. Capture `NewRecipe` for title correction, `["200 g dark chocolate", "4 eggs"]`, corrected preparation `"Chill."`, and notes `"Serve chilled."`; expect `["7.1 oz (200 g) dark chocolate", "4 eggs"]` and `"Chill.\n\nNotes: Serve chilled."`. Assert all NewRecipe fields and returned detail ID 101/category name from fake save. Test:
- null category → lookup `[15]`; proposed 7 → `[7]`; override 8 beats proposed 7 → `[8]`; missing 99 → `[99,15]`; missing default → `[15]` and no save; existing 7 succeeds without category 15.
- preconverted ingredient unchanged; corrected metric text overrides stale conversion metadata; UI-null cleared fields fall back.
- source null defaults, empty source remains empty, explicit source remains exact; raw response null/JSON/empty remains exact; fixed Instant and zero initial views.
- invalid title/list/preparation performs no repository calls; write failure propagates with no output, one attempted save, and unchanged command/proposal/overrides; category lookup failure propagates with no save.
Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.ConfirmRecipeImportUseCaseTest`, then the complete core suite `./gradlew :core:domain:test :core:usecases:test :backend:compileJava` to check the shared contracts and temporary wrapper. No Quarkus tests in core, database, or live AI.

## Dependencies / Preconditions
None for core/unit work. Category 15 must exist in deployed storage for the current UI's default import to succeed; the use case must fail if it does not, rather than creating/seed-fixing it.
