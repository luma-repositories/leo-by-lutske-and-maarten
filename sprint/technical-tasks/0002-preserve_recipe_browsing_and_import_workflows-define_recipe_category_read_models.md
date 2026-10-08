# Title
Define pure recipe and category read models

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — browsing requirements and AC1–AC3; eventual story path.

## Objective
Represent existing browse/detail data without exposing persistence entities or REST DTOs to core.

## Scope
Four public Java records in `be.lutske.leolegacy.domain`, retaining the current ID, count, and text types.

## Out of Scope
Repositories, queries, conversion, validation of historical content, serialization annotations, ORM, schema changes, and frontend changes.

## Clean Architecture Placement
`:core:domain`, JDK-only production code. Existing backend response records remain outer DTOs for later mapping.

## Execution Dependencies
- `0001-preserve_recipe_browsing_and_import_workflows-create_core_gradle_modules.md`

## Implementation Details
Define these exact public record signatures (canonical constructors/accessors are the public API):

```java
record Category(long id, String name) {}
record CategoryWithCount(long id, String name, long recipeCount) {}
record RecipeSummary(long id, String title, String categoryName, int viewCount) {}
record RecipeDetail(long id, String title, List<String> ingredients,
                    String preparation, long categoryId, String categoryName,
                    int viewCount) {}
```

Use `java.util.List`. Preserve ordered ingredient strings, preparation newlines, names, zero view counts, and long category counts without trimming, conversion, sorting, or new content validation. Read adapters supply non-null ingredient lists, including empty lists; `RecipeDetail` may defensively copy that list with `List.copyOf`. Do not convert `servings` into a number elsewhere or widen `viewCount` to long. `Category` is the lookup result used for choosing an import category; `CategoryWithCount` is the sidebar projection. `RecipeSummary` intentionally has no category ID, matching `RecipeSummaryResponse`. The detail carries category ID for return navigation. Future JDBC adapters decode pipe-separated stored ingredients by splitting on `\\|`, dropping blank entries, retaining the remaining strings and order, as `RecipeResource.toDetail` currently does; that decoding is not domain logic.

## Files / Modules Impacted
- `core/domain/src/main/java/be/lutske/leolegacy/domain/Category.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/CategoryWithCount.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/RecipeSummary.java`
- `core/domain/src/main/java/be/lutske/leolegacy/domain/RecipeDetail.java`
- `core/domain/src/test/java/be/lutske/leolegacy/domain/RecipeReadModelsTest.java`

## Acceptance Criteria
- Given a saved detail with ordered ingredients and multiline preparation, when represented by `RecipeDetail`, then all fields retain their values and order.
- Given a category count above `Integer.MAX_VALUE` and a zero-view summary, when constructing projections, then the long count and integer zero are preserved.
- Given an empty ingredient list, when constructing a historical detail, then no new required-field validation rejects it.

## Testing Requirements
Plain JUnit fixtures: category `(15L, "Imported")`, count `3_000_000_000L`, summary with `viewCount=0`, detail ingredients `["  flour", "4 eggs"]` and preparation `"Mix.\nBake."`, and an empty historical list. Assert exact values, types through compile-time constructors, and order. Run `./gradlew :core:domain:test --tests be.lutske.leolegacy.domain.RecipeReadModelsTest`. No Quarkus tests.

## Dependencies / Preconditions
None.
