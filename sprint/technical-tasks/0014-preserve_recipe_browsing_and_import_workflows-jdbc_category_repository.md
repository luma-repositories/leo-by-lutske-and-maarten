# Title
Implement JDBC category counts and lookup

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — alphabetical sidebar and existing import categories; eventual story location.

## Objective
Implement the core category port with one aggregate list query and an optional lookup.

## Scope
`JdbcCategoryRepository implements be.lutske.leolegacy.usecases.CategoryRepository` with the exact task-0004 signatures.

## Out of Scope
REST producers, category creation, recipe entity loading, per-category counting, schema/data migrations, destructive DB operations, and new ORM code.

## Clean Architecture Placement
Outer `:backend`, package `be.lutske.leolegacy.infrastructure.persistence.jdbc`. Use `@ApplicationScoped` and constructor-injected `javax.sql.DataSource`; only the adapter knows JDBC/CDI. Return domain `CategoryWithCount` and `Category`.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
Use these exact table/column names from V1:

```sql
SELECT c.id, c.name, COUNT(r.id) AS recipe_count
FROM category c LEFT JOIN recipe r ON r.category_id = c.id
GROUP BY c.id, c.name ORDER BY c.name
```

`listWithCounts()` reads IDs/counts with `getLong`, name with `getString`; `COUNT(r.id)`, not `COUNT(*)`, gives zero for empty categories. Database name ordering is authoritative. Return a non-null list, including an empty list if no categories exist.

```sql
SELECT id, name FROM category WHERE id = ?
```

`findById(long categoryId)` uses `PreparedStatement.setLong(1, categoryId)` and returns `Optional.empty()` on no row. Do not reject/coerce zero/negative IDs. Try-with-resources closes connection, statement, and result set on success and failure. Wrap `SQLException` in a contextual unchecked exception preserving cause; failures must not become empty lists/absence. No write transaction or view changes. Do not confuse this port with the temporary old repository of the same simple name in `infrastructure.persistence.repository`.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcCategoryRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcCategoryRepositoryTest.java`

## Acceptance Criteria
- Given populated and empty categories, when listing, then alphabetical names and accurate long counts include zero.
- Given present/absent category IDs, when looking up, then the exact category/empty Optional is returned.
- Given a SQL failure, when querying, then the failure propagates with its cause and all acquired resources close.

## Testing Requirements
Plain JDBC/H2 adapter tests using a uniquely named in-memory database in PostgreSQL mode, initialized with unchanged Flyway V1–V4; no compose connection. Insert test-owned categories with bound parameters and recipes with generated IDs, assert aggregate results against direct SQL, unknown/zero IDs and resource failure behavior. Keep fixture setup local to this test until task 0024 supplies shared support. Do not add ORM fixtures. Run `./gradlew :backend:test --tests be.lutske.leolegacy.infrastructure.persistence.jdbc.JdbcCategoryRepositoryTest -x :backend:buildFrontend -x :backend:copyFrontend`. A recording JDBC double may cover counts above integer range without billions of fixture rows.

## Dependencies / Preconditions
Existing test-scoped `quarkus-jdbc-h2`, Flyway, and JDK 25; no external DB.
