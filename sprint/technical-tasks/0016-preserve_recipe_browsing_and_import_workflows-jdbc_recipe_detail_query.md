# Title
Complete JDBC recipe details without view increments

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — saved detail preservation and return category; eventual story location.

## Objective
Complete the same JDBC read adapter so it can safely be wired to all browsing use cases.

## Scope
Replace the task-0015 detail placeholder with optional joined detail lookup and codec mapping.

## Out of Scope
View counting, reconversion, import persistence, HTTP mapping, new entities, and schema/data migrations.

## Clean Architecture Placement
Outer `JdbcRecipeReadRepository` implements the pure core port; decoding stays in `LegacyIngredientCodec`. No framework types leak into `RecipeDetail`.

## Execution Dependencies
- `0013-preserve_recipe_browsing_and_import_workflows-legacy_ingredient_codec.md`
- `0015-preserve_recipe_browsing_and_import_workflows-jdbc_recipe_summary_queries.md`

## Implementation Details
`Optional<RecipeDetail> findById(long recipeId)` executes:

```sql
SELECT r.id, r.title, r.ingredients, r.preparation, r.category_id,
       c.name AS category_name, r.view_count
FROM recipe r JOIN category c ON c.id = r.category_id WHERE r.id = ?
```

Bind `setLong(1, recipeId)` without positivity checks. Map long ID/category ID, integer views, exact text, and codec-decoded ingredient strings into the task-0002 component order. Return `Optional.empty()` only for no row. Reads execute no UPDATE, increment, reset, transaction-triggered write, or conversion. Preserve preparation whitespace/newlines and surviving ingredient whitespace. Close all JDBC resources, propagate failures with SQL cause, and remove the temporary `UnsupportedOperationException` entirely. The NOT NULL FK in V1 justifies the category inner join; no invented category name/default on read.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeReadRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeReadRepositoryTest.java`

## Acceptance Criteria
- Given historical pipe ingredients and multiline preparation, when reading, then detail preserves values/order and category navigation ID.
- Given repeated detail reads, when comparing the stored row before/after, then view count and every stored field remain unchanged.
- Given an absent row or a SQL failure, when reading, then absence and failure remain distinguishable.

## Testing Requirements
Extend the existing H2/JDBC test with `"200 g flour| | 4 eggs |"`, `"Mix.\n\nBake."`, empty ingredients, nonzero views, missing/zero IDs, and SQL failure. Snapshot the raw row, read twice, compare the raw row afterward; assert no conversion to imperial units. Run `./gradlew :backend:test --tests be.lutske.leolegacy.infrastructure.persistence.jdbc.JdbcRecipeReadRepositoryTest -x :backend:buildFrontend -x :backend:copyFrontend` and `./gradlew :backend:compileJava -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing H2/Flyway test dependencies; no external database.
