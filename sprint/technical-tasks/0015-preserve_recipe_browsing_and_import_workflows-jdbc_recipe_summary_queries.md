# Title
Implement JDBC recipe summary queries

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — all/category browsing and top ten; eventual story location.

## Objective
Move summary projection and popular ordering/limiting into SQL without loading entities.

## Scope
Create `JdbcRecipeReadRepository` implementing the full core `RecipeReadRepository` interface; implement its three summary methods now.

## Out of Scope
Detail implementation (0016), CDI use-case wiring, view writes, pagination, tie-breakers, schema changes, and new ORM functionality.

## Clean Architecture Placement
Outer `:backend` JDBC adapter, `@ApplicationScoped`, constructor-injected `DataSource`. Core port and `RecipeSummary` stay framework-free.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Shared projection SQL:

```sql
SELECT r.id, r.title, c.name AS category_name, r.view_count
FROM recipe r JOIN category c ON c.id = r.category_id
```

- `listAll()` uses that query with no ORDER BY or limit. Return all summaries, not the popular list.
- `findByCategoryId(long categoryId)` appends `WHERE r.category_id = ?`, binds `setLong(1, categoryId)`, and returns an empty list for unknown/empty categories without another category query or ID validation.
- `findTopByViewCount(int limit)` appends `ORDER BY r.view_count DESC LIMIT ?` and binds `setInt(1, limit)`. The port requires a positive limit; fail fast with `IllegalArgumentException` for nonpositive direct adapter calls. The use case supplies 10. No Java fetch-all sorting/limiting or secondary tie order.
- Map `getLong("id")`, `getString("title")`, `getString("category_name")`, `getInt("view_count")` to the exact four-component record. Lists never return null.
- Close connection/statement/result set with try-with-resources; propagate SQL failures as contextual unchecked exceptions with causes, not empty successes.
- **Intermediate compilation choice:** implement `Optional<RecipeDetail> findById(long recipeId)` temporarily by throwing `UnsupportedOperationException("Detail query is implemented in task 0016")`. This is solely a short-lived compile bridge, not a port behavior to release. Do not wire any browsing use case to this bean until 0016 removes it. Avoid an abstract base class or another provisional interface.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeReadRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeReadRepositoryTest.java`

## Acceptance Criteria
- Given more than ten rows, when querying top with 10, then SQL returns at most ten in descending count order.
- Given a category or no filter, when querying, then only the selected category or all recipes are returned with unchanged fields.
- Given the intermediate class, when compiling before 0016, then it satisfies the interface and no REST producer references it yet.

## Testing Requirements
Use a private H2 PostgreSQL-mode database with unchanged Flyway migrations and JDBC-only fixtures. Add 12 rows whose distinct high view counts exceed seed counts, zero-count rows, and tied counts. Compare all/category sets without imposing order; test positive limits 1 and 10, unknown category and SQL failure. Inspect/record SQL binding to prove `LIMIT ?` is executed rather than Java truncation. Keep fixtures local until 0024. Run `./gradlew :backend:compileJava :backend:test --tests be.lutske.leolegacy.infrastructure.persistence.jdbc.JdbcRecipeReadRepositoryTest -x :backend:buildFrontend -x :backend:copyFrontend`. Do not exercise the detail placeholder as if it were completed behavior.

## Dependencies / Preconditions
Existing H2 JDBC/Flyway test dependencies and JDK 25; no external database.
