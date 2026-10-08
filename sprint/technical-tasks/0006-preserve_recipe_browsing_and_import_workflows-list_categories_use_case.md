# Title
Implement ListCategoriesUseCase

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC2 and alphabetical sidebar counts; eventual story path.

## Objective
Expose the existing alphabetical category/count list through one pure read use case.

## Scope
One constructor-injected use case using `CategoryRepository.listWithCounts()`.

## Out of Scope
Database queries, ORM, REST/CDI rewiring, fetching recipes to count them, caching, UI changes, and new collation rules.

## Clean Architecture Placement
Public plain class `be.lutske.leolegacy.usecases.ListCategoriesUseCase` in `:core:usecases`. Only the core repository and domain projection are referenced; outer Quarkus wiring follows later.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Exact public API:

```java
public ListCategoriesUseCase(CategoryRepository categoryRepository);
public List<CategoryWithCount> execute();
```

Store the constructor dependency in a final field. `execute()` calls `listWithCounts()` once and returns its projection list preserving order, names, IDs, and long counts. The port owns alphabetical query ordering and accurate aggregate counts, including zero-count categories. Do not sort again with a Java comparator that could change database collation; do not call `findById` or use `RecipeReadRepository`. Empty collection remains an empty list. Propagate repository failures rather than fabricating an empty successful result. No framework annotations.

## Files / Modules Impacted
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ListCategoriesUseCase.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/ListCategoriesUseCaseTest.java`

## Acceptance Criteria
- Given ordered categories with counts, when executing, then the same ordered values are returned, including an empty category's zero count.
- Given no categories, when executing, then an empty list is returned.
- Given a repository failure, when executing, then the failure propagates instead of showing false empty success.

## Testing Requirements
Hand-written `CategoryRepository` fake returns `[CategoryWithCount(1,"Baking",2), CategoryWithCount(15,"Imported",0), CategoryWithCount(9,"Soups",3_000_000_000L)]`, counts `listWithCounts` invocations, and throws if `findById` is called. Assert exact order/counts, one call, empty result, and propagated failure. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.ListCategoriesUseCaseTest`. Ordinary JUnit only, no Quarkus/database.

## Dependencies / Preconditions
None.
