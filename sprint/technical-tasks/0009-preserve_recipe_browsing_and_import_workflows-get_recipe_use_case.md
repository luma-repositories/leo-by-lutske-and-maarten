# Title
Implement GetRecipeUseCase as an optional read with no write

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC3 and no new view-counting behavior; eventual story path.

## Objective
Return the saved recipe detail or absence without modifying the recipe.

## Scope
One detail-read use case backed by `RecipeReadRepository.findById`.

## Out of Scope
View increments, save operations, not-found HTTP mapping, conversion of existing data, storage decoding, ORM/JDBC implementations, and frontend navigation changes.

## Clean Architecture Placement
Public plain `be.lutske.leolegacy.usecases.GetRecipeUseCase` in `:core:usecases`; returns domain detail in `java.util.Optional`. No REST exceptions or annotations.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Exact public API:

```java
public GetRecipeUseCase(RecipeReadRepository recipeReadRepository);
public Optional<RecipeDetail> execute(long recipeId);
```

Delegate once to `findById(recipeId)` and return its Optional. Do not impose an ID validation rule; transport the supplied long unchanged. Preserve saved title, ordered ingredients, preparation whitespace/newlines, category ID/name, and view count. Neither constructor nor method has a write dependency. Absence is `Optional.empty()` for later REST mapping to the established not-found response; repository failure propagates and is not converted into absence. Do not increment or reset view count. Do not reconvert old metric text.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/GetRecipeUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/GetRecipeUseCaseTest.java`

## Acceptance Criteria
- Given recipe 42, when opened repeatedly, then both reads return the same saved detail and recorded view count with no mutation.
- Given missing recipe 999, when executing, then Optional is empty.
- Given repository failure, when executing, then failure remains distinguishable from not found.

## Testing Requirements
Hand-written read fake records IDs; all list methods throw if called. Detail fixture: ID 42, category 15/Imported, ingredients `["200 g flour", " 4 eggs "]`, preparation `"Mix.\n\nBake."`, view count 7. Assert exact field/order preservation on two reads and no changes to the fixture; test 999 absence, 0L argument passthrough, and propagated failure. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.GetRecipeUseCaseTest`. Plain JUnit only.

## Dependencies / Preconditions
None.
