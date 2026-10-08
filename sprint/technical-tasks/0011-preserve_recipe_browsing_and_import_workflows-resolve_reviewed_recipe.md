# Title
Implement ResolveReviewedRecipe as a pure merge and validation helper

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — requirements 6–7 and 9, AC7, and required-field fallback scenario; eventual story path.

## Objective
Extract the current backend merge/validation/notes rules without confusing non-null API overrides with the UI's cleared-field behavior.

## Scope
One stateless pure helper returning a merged `ProposedRecipe`, validated for confirmation, before category resolution/conversion/persistence.

## Out of Scope
Conversion, category lookup/defaulting, clock, persistence, new field validation, UI changes, text-area parsing in core, and HTTP exceptions.

## Clean Architecture Placement
Public final class `be.lutske.leolegacy.domain.ResolveReviewedRecipe` in `:core:domain`; no dependencies. Use a private constructor and a public static pure method. It is a reusable domain operation, not an injectable application service.

## Execution Dependencies
- `0003-preserve_recipe_browsing_and_import_workflows-define_import_extraction_models.md`

## Implementation Details
Exact public API:

```java
public static ProposedRecipe resolve(ProposedRecipe proposedRecipe,
                                     UserOverrides userOverrides);
```

Require a non-null proposal at the calling boundary; overrides may be null. Reproduce `RecipeImportResource.confirmImport` in this order:
1. Title, ingredients, preparation, and category ID individually choose the override when **non-null**, otherwise the corresponding proposal value. An empty or whitespace String and an empty list are non-null overrides: do not silently fall back for these direct API values.
2. Notes have a different rule: if the overrides object exists, use its `notes()` even when null; only when the entire overrides object is null use proposed notes. Thus an existing overrides object with null notes suppresses proposed notes.
3. Validate resolved title null/`isBlank()` → `RecipeImportValidationException("Title is required")`; then resolved ingredients null/`isEmpty()` → `RecipeImportValidationException("Ingredients are required")`; then resolved preparation null/`isBlank()` → `RecipeImportValidationException("Preparation is required")`. Preserve this validation order and shallow list check; do not filter list elements or add blanket trimming here.
4. If selected notes are non-null and not blank, append exactly `"\n\nNotes: " + notes` to preparation. Preserve nonblank note whitespace; blank/null notes append nothing.
5. Return a new `ProposedRecipe` with resolved title/ingredients/preparation/category, original description/servings/source/tags/convertedIngredients, and `notes=null` because notes have been folded into preparation. Do not apply `UserOverrides.servings` or `.description`; the existing API accepts these fields but the current merge ignores them. Category stays nullable here for task 0012 to resolve.

UI compatibility is a boundary fact, not a different core merge: `ImportRecipePage` splits ingredient edits by newline, trims each line, drops empties, and sends null if no entries remain. It sends null for an empty preparation String (a whitespace-only String remains non-null and fails validation). Therefore cleared ingredients/preparation fall back to proposed values, while direct API `[]`/`""` overrides fail validation. The UI disables confirmation for blank/whitespace title; backend validates the resolved title. A direct null title override still falls back to the proposal, matching the existing API; do not replace that behavior with an unconditional override-title rejection. No new UI selector or normalization is introduced.

## Files / Modules Impacted
- `core/domain/src/main/java/be/lutske/leolegacy/domain/ResolveReviewedRecipe.java`
- `core/domain/src/test/java/be/lutske/leolegacy/domain/ResolveReviewedRecipeTest.java`

## Acceptance Criteria
- Given a complete proposal and non-null corrections, when resolving, then corrections replace proposed title/ingredients/preparation/category and nonblank notes are appended once.
- Given UI-style null cleared ingredients/preparation, when resolving, then proposed values are retained; if both sources lack required content, validation fails.
- Given explicit empty ingredients or blank title/preparation overrides, when resolving, then the matching validation failure occurs rather than fallback.
- Given an overrides object with null notes, when resolving, then proposed notes are suppressed; with no overrides object, proposed notes are used.

## Testing Requirements
JUnit fixture proposal `Cake`, ingredients `["200 g flour", "4 eggs"]`, preparation `"Mix."`, notes `"Serve chilled."`, category 15, servings `"4"`, description `"Original"`. Table-test null overrides, individual null overrides, replacement lists, explicit empty list, empty/whitespace title/preparation, all missing values with validation order, and UI-normalized `["200 g dark chocolate", "4 eggs"]` from the documented spaced/blank-line input. Assert no further list parsing/normalization. Test proposed notes with no overrides, null/blank override notes suppression, nonblank whitespace-preserving notes appended after a blank line, `notes=null` in result, ignored description/servings overrides, category override 7, nullable category passthrough, and unchanged input records. No conversion expected yet. Run `./gradlew :core:domain:test --tests be.lutske.leolegacy.domain.ResolveReviewedRecipeTest`. Plain JUnit only.

## Dependencies / Preconditions
None.
