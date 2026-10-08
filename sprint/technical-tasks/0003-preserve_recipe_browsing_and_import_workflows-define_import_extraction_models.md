# Title
Define pure import, extraction, and exception models

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC4–AC5 and reviewed import requirements; eventual story path.

## Objective
Expose framework-free import data with exactly the existing field types and completeness semantics.

## Scope
Public domain records for extraction, proposal, overrides, review result, and conversion; pure exceptions for extraction and confirmation failures.

## Out of Scope
Conversion implementation, merge logic, repositories, HTTP status codes, upload validation, AI parsing/prompt changes, and replacement of old backend models.

## Clean Architecture Placement
All types are in `be.lutske.leolegacy.domain` in `:core:domain`. Only JDK types; no Jackson, Jakarta, Quarkus, LangChain4j, or logging. Legacy service records may coexist until adapter migration.

## Execution Dependencies
- `0001-preserve_recipe_browsing_and_import_workflows-create_core_gradle_modules.md`

## Implementation Details
Create these public records with component order fixed as shown (`List` means `java.util.List`):

```java
record IngredientConversion(String originalQuantity, String convertedQuantity,
    String unit, String ingredientName, String displayValue, boolean converted) {}
record ExtractionResult(String title, String description, String servings,
    List<String> ingredients, List<String> steps, String source, List<String> tags,
    List<String> warnings, String rawModelResponse, String provider, String model) {
    public ExtractionResult();
    public List<String> missingFields();
    public boolean isComplete();
}
record ProposedRecipe(String title, String description, String servings,
    List<String> ingredients, String preparation, String source, List<String> tags,
    Long categoryId, String notes, List<IngredientConversion> convertedIngredients) {}
record UserOverrides(String title, List<String> ingredients, String preparation,
    Long categoryId, String notes, String servings, String description) {}
record RecipeReviewResult(String status, String rawModelResponse,
    ProposedRecipe proposedRecipe, List<String> missingFields, List<String> warnings) {}
```

These are signatures, not record method implementations. Copy `ExtractionResult` behavior from the existing backend service record: null warnings become `List.of()`; the no-arg constructor supplies all nulls except empty warnings. `missingFields()` returns names in order `title`, `ingredients`, `preparation`: title null/blank, ingredients null/empty, steps null/empty respectively. `isComplete()` is `missingFields().isEmpty()`. Do not strengthen it to inspect blank elements inside a non-empty list. Other nullable components remain nullable; do not normalize proposal or override nulls/empties. Preserve `String servings`, nullable `Long categoryId`, provider/model metadata, and conversion boolean. Review status is the existing String `"COMPLETE"` or `"NEEDS_MORE_INFO"`, not a save result or new enum.

Define public unchecked exception classes and constructor signatures:

```java
class RecipeExtractionException extends RuntimeException {
    public RecipeExtractionException(String message);
    public RecipeExtractionException(String message, Throwable cause);
}
class RecipeImportValidationException extends RuntimeException {
    public RecipeImportValidationException(String message);
}
class ImportCategoryNotFoundException extends RuntimeException {
    public ImportCategoryNotFoundException();
}
```

The last constructor uses `"Default import category not found"`. Validation messages are supplied by task 0011. No HTTP semantics in exceptions. Persistence failures remain propagated runtime failures from a future adapter; do not introduce a success-shaped error result.

## Files / Modules Impacted
- `core/domain/src/main/java/be/lutske/leolegacy/domain/IngredientConversion.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/ExtractionResult.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/ProposedRecipe.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/UserOverrides.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/RecipeReviewResult.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/RecipeExtractionException.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/RecipeImportValidationException.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/ImportCategoryNotFoundException.java`
- `core/domain/src/test/java/be/lutske/leolegacy/domain/ImportModelsTest.java`

## Acceptance Criteria
- Given empty extraction, when checking completeness, then all three ordered missing-field names are returned and warnings are non-null and empty.
- Given title, one ingredient, and one step, when checking completeness, then it is complete without implying persistence.
- Given nullable overrides, when constructing records, then null, empty, and blank remain distinguishable for the merge helper.

## Testing Requirements
JUnit fixtures: no-arg extraction; whitespace title; ingredients present with steps absent; fully complete result with `servings="4–6 servings"`; non-empty lists containing blank strings (retain current shallow completeness); null warnings; proposal/overrides with null vs empty ingredients and preparation. Assert extraction exception preserves cause and both exception message contracts. Run `./gradlew :core:domain:test --tests be.lutske.leolegacy.domain.ImportModelsTest`. No Quarkus tests.

## Dependencies / Preconditions
None.
