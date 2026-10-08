# Title
Implement ListRecipesUseCase with nullable category filtering

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — category browsing and existing recipe-list API; eventual story path.

## Objective
Preserve the distinction between all recipes, category-filtered recipes, and the separate popular query.

## Scope
One read use case branching on nullable `Long categoryId`.

## Out of Scope
Popular ranking, category existence checks, pagination, new list ordering, JDBC/ORM, REST wiring, view increments, and UI changes.

## Clean Architecture Placement
Public plain `be.lutske.leolegacy.usecases.ListRecipesUseCase` in `:core:usecases`, depending only on `RecipeReadRepository` and domain summaries.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Exact public API:

```java
public ListRecipesUseCase(RecipeReadRepository recipeReadRepository);
public List<RecipeSummary> execute(Long categoryId);
```

Null means call `listAll()` once; non-null means call `findByCategoryId(categoryId)` once with unboxed long. This preserves `RecipeResource.listRecipes`; null is not shorthand for the popular list and is not default category 15. Do not validate or coerce IDs (including zero or negative values) and do not load categories first. Preserve repository order and summary fields, including zero counts. Unknown/empty categories return the port's empty list. Propagate failures. No sorting, truncating, conversion, writes, framework annotations, or extra dependencies.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ListRecipesUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/ListRecipesUseCaseTest.java`

## Acceptance Criteria
- Given null category ID, when executing, then all-list query alone is invoked and its entire result is returned.
- Given category 15, when executing, then only its category query is invoked and returned summaries retain their values/order.
- Given an unknown category, when executing, then an empty list remains a successful empty list.

## Testing Requirements
Use a recording `RecipeReadRepository` fake with distinct all/category fixtures (12 all-list summaries to expose accidental limiting), two category-15 summaries with zero/positive views, and category 999 returning empty. Make popular/detail methods fail if called. Test null, 15L, 999L, and 0L exact argument transport; assert one selected call, no other calls, complete unmodified results, and repository exception propagation. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.ListRecipesUseCaseTest`. Plain JUnit only.

## Dependencies / Preconditions
None.
