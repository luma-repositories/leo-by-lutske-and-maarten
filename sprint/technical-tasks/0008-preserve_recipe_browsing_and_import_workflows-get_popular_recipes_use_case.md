# Title
Implement GetPopularRecipesUseCase with the existing ten-recipe limit

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC1 and equal-count/fewer-than-ten edge cases; eventual story path.

## Objective
Request up to ten recipes in descending recorded view-count order using an explicit popular query.

## Scope
One read use case issuing `findTopByViewCount(10)`.

## Out of Scope
New tie-breakers, view-count writes, fetch-all sorting, pagination, adapter query implementation, ORM, REST/CDI wiring, and UI changes.

## Clean Architecture Placement
Plain public class `be.lutske.leolegacy.usecases.GetPopularRecipesUseCase` in `:core:usecases`. The repository contract, not core persistence code, defines ordering/limiting for later JDBC.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Exact public API:

```java
public GetPopularRecipesUseCase(RecipeReadRepository recipeReadRepository);
public List<RecipeSummary> execute();
```

Call `findTopByViewCount(10)` exactly once, returning the port result unchanged. The port must select at most ten descending `int viewCount` values; ties are unspecified. Do not call `listAll()` and sort in core or silently repair a broken adapter. Fewer than ten recipes (including zero) return all available results. Zero counts stay in data; their visual suppression belongs to the existing UI. Failures propagate. No annotations or other dependencies.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/GetPopularRecipesUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/GetPopularRecipesUseCaseTest.java`

## Acceptance Criteria
- Given twelve recipes with distinct counts, when executing against a contract-compliant repository, then only the ten highest in descending count order are returned and requested limit is 10.
- Given two available recipes or none, when executing, then all available results are returned.
- Given equal counts, when executing, then no additional secondary order is imposed.

## Testing Requirements
Recording fake owns twelve fixtures with view counts 0–11 and applies the port's descending/limit semantics; assert requested limit 10 and counts 11–2. Also return an already-ordered tied fixture in intentionally non-ID order to assert pass-through, plus two-item (including zero) and empty fixtures. Other read methods throw if called. Test failure propagation. Adapter-side query correctness remains a later adapter test. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.GetPopularRecipesUseCaseTest`. Plain JUnit only, no Quarkus/database.

## Dependencies / Preconditions
None.
