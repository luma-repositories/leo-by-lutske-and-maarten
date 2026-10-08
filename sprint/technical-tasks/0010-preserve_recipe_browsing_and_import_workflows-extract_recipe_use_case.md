# Title
Implement ExtractRecipeUseCase returning an editable review proposal only

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC4–AC6 and complete/incomplete/empty review requirements; eventual story path.

## Objective
Extract image content through a port, convert proposed ingredients, and always return review data without saving.

## Scope
Pure extraction orchestration and mapping from `ExtractionResult` into `RecipeReviewResult`.

## Out of Scope
Saving, auto-confirmation, file reading, multipart handling, upload/extension validation, AI prompt/parser implementation, retries, HTTP mapping, category lookup, and UI changes.

## Clean Architecture Placement
Public plain `be.lutske.leolegacy.usecases.ExtractRecipeUseCase` in `:core:usecases`; repository and pure domain converter are constructor dependencies. No write repository, persistence entities, filesystem paths, or framework annotations.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`
- `0005-preserve_recipe_browsing_and_import_workflows-move_ingredient_conversion_to_domain.md`

## Implementation Details
Exact public API (`IngredientConverter` is the domain class):

```java
public ExtractRecipeUseCase(RecipeExtractionRepository recipeExtractionRepository,
                            IngredientConverter ingredientConverter);
public RecipeReviewResult execute(byte[] imageBytes, String mimeType);
```

Call `extractRecipeFromImage(imageBytes, mimeType)` exactly once with the supplied bytes/MIME. Use the returned extraction to build a fresh proposal:
1. Copy title, description, String servings, source, and tags unchanged.
2. Preparation is null for null steps, otherwise `String.join("\n", steps)` (empty list gives empty String).
3. Run `ingredientConverter.convertAll(extraction.ingredients())`. `convertedIngredients` is this list; displayed ingredients are its ordered `displayValue`s when non-empty, otherwise the original ingredients if non-null or `List.of()` if null. This matches `RecipeImportResource.enrichWithConversions`.
4. Proposal `categoryId` and `notes` are null; do not assign a category during extraction.
5. Status is `extraction.isComplete() ? "COMPLETE" : "NEEDS_MORE_INFO"`. Missing fields come from the original extraction's `missingFields()`, not reconstructed converted proposal fields. Preserve warnings and raw model response exactly. Provider/model stay on `ExtractionResult`, not newly exposed as review fields.

Complete, partial, no-recipe, and malformed-JSON fallback extractions all return the same editable review result shape. Nothing saves or produces a saved ID. A `RecipeExtractionException` propagates; do not treat provider failure as successful empty review. English translation, uncertainty warnings, and non-invention are extraction adapter obligations already expressed in the existing LangChain4j prompt, not additional core heuristics.

Boundary handoff for later adapters: current REST rejects sizes greater than `10L * 1024 * 1024` (exact boundary allowed), validates MIME when supplied against `image/png`, `image/jpeg`, `image/jpg`, `image/webp`, and separately validates case-insensitive extensions png/jpg/jpeg/webp. It uses `image/png` when content type is null. Keep these checks and file reading outside this bytes/MIME use case; do not add stricter core MIME checks or filename parameters here.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ExtractRecipeUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/ExtractRecipeUseCaseTest.java`

## Acceptance Criteria
- Given a complete extraction, when executing, then status is COMPLETE and the editable proposal is returned without a saved recipe.
- Given missing preparation or no recipe content, when executing, then NEEDS_MORE_INFO includes ordered missing fields, available content, raw text, and warnings.
- Given provider failure, when executing, then the exception propagates and no review/success result is returned.

## Testing Requirements
Plain JUnit with a recording extraction fake and real domain converter: bytes `{1,2,3}`/`image/jpeg`; complete title/ingredients `["200 g dark chocolate", "4 eggs"]`/steps `["Mix.", "Chill."]`; partial result with null steps; no-arg empty result; malformed-JSON fallback represented as empty content with raw text and a warning; null vs empty steps; preconverted ingredients; extraction exception with cause. Assert exact `7.1 oz (200 g) dark chocolate`, joined preparation, null category/notes, unchanged metadata/warnings, one port call, and bytes/MIME fidelity. A byte array exactly `10 * 1024 * 1024` must pass through (actual multipart boundary/rejection tests belong to later REST tasks). Constructor has no write capability. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.ExtractRecipeUseCaseTest`. No Quarkus, network, or filesystem fixtures.

## Dependencies / Preconditions
None; unit tests use an in-memory extraction fake, not AI credentials.
