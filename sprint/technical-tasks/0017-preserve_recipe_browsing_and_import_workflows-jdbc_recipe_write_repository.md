# Title
Implement atomic JDBC recipe insertion with committed-success output

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — reviewed save and failure recovery; eventual story location.

## Objective
Make `RecipeWriteRepository.save(NewRecipe)` return an actual persisted detail only after a successful local JDBC commit.

## Scope
Explicit INSERT, generated ID retrieval, same-connection detail mapping, commit, rollback, and resource cleanup in `JdbcRecipeWriteRepository`.

## Out of Scope
Review merging/conversion, category fallback/creation, HTTP responses, JTA orchestration, updates/upserts, ID allocation in Java, schema/data migrations, and ORM.

## Clean Architecture Placement
Outer `:backend`, `be.lutske.leolegacy.infrastructure.persistence.jdbc`, `@ApplicationScoped`, constructor-injected `DataSource`. Implements the exact core write port; no transaction annotation or JDBC in core. This adapter owns its local transaction; callers must not surround it with an ambient JTA transaction.

## Execution Dependencies
- `0013-preserve_recipe_browsing_and_import_workflows-legacy_ingredient_codec.md`

## Implementation Details
Use the V1–V4 columns exactly:

```sql
INSERT INTO recipe
  (title, ingredients, preparation, category_id, view_count, source, created_at, import_metadata)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

- Acquire a connection, record its autocommit state, set autocommit false, and prepare with `Statement.RETURN_GENERATED_KEYS`. Bind 1 title/2 codec-encoded ingredients/3 preparation using `setString`; 4 `setLong(categoryId)`; 5 `setInt(viewCount)`; 6 source with `setString` or `setNull(Types.VARCHAR)`; 7 `setTimestamp(Timestamp.from(createdAt), UTC Calendar)` or typed `Types.TIMESTAMP` null if supplied; 8 metadata with `setString` or `setNull(Types.LONGVARCHAR)`. Do not serialize metadata as JSON or change its text.
- V3 `created_at` is TIMESTAMP without time zone, not TIMESTAMPTZ. Bind/read with an explicit UTC calendar consistently, preserving the instant to DB precision; no session-default-timezone conversion. V3 source is nullable VARCHAR(100); V4 metadata is nullable TEXT. Do not truncate title/source to bypass constraints.
- Require exactly one affected row and one usable generated ID (`getLong(1)` plus null/missing-key check). Never use `MAX(id)+1`, manually supplied recipe IDs, sequence reset, or retry on collision. V3 already established identities; the adapter only consumes generated values.
- Before commit, on this same connection query the inserted ID with `SELECT r.id, r.title, r.ingredients, r.preparation, r.category_id, c.name AS category_name, r.view_count FROM recipe r JOIN category c ON c.id = r.category_id WHERE r.id = ?`; bind the generated long. Build the seven-field detail with `LegacyIngredientCodec.decode`. Missing joined row or mapping failure is a save failure. Do not call a read repository opening another connection before commit.
- Close statements/result sets before commit; commit explicitly before returning the mapped detail. Track an acknowledged successful commit only when `commit()` returns normally. A commit failure must throw and must never expose an optimistic result. Roll back any pre-commit insert/key/mapping/runtime/SQL failure and attempt rollback after an unacknowledged commit failure. Preserve the primary exception and add rollback/cleanup failures as suppressed exceptions.
- Release the connection on all paths. Restore original autocommit only after successful commit or successful rollback; never toggle it to true after a failed rollback (that could commit work). On rollback failure, explicitly invalidate the physical pooled connection through the pool-supported invalidation mechanism or supported JDBC `abort`; ordinary pooled `close()` alone is not disposal. Ensure the unusable connection cannot return to the reusable pool, even if subsequent release fails. Autocommit-restoration or close failure also requires invalidation/abort, not reuse.
- After acknowledged successful commit, return the known saved detail even if autocommit restoration or connection cleanup fails: log the cleanup/invalidation failure with its cause in this outer JDBC adapter, invalidate/abort the connection, and preserve committed success. Never roll back after acknowledged commit or report a retryable save failure for cleanup alone. On failed operations cleanup must not turn failure into success. Do not claim that a network-ambiguous commit can be undone or promise request deduplication. Keep this lifecycle handling local to the adapter; add no retry/deduplication or application feature.
- Inputs are the validated `NewRecipe` from core; adapter enforces SQL constraints, not alternate review rules. Category deletion races fail the FK/lookup and roll back. Wrap SQL failures as unchecked exceptions preserving causes.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeWriteRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/JdbcRecipeWriteRepositoryTest.java`

## Acceptance Criteria
- Given valid input, when save returns, then another connection sees exactly one new row and the returned generated ID/category/detail match it.
- Given an invalid FK, oversized title/source, failed key retrieval, mapping failure, or failed commit, when saving, then no successful detail escapes and rollback is attempted appropriately.
- Given nullable source/metadata and a fixed Instant, when saving, then SQL values preserve null/text/time semantics and all resources are released.
- Given rollback, autocommit restoration, or close failure, when transaction cleanup runs, then an unusable physical connection is invalidated/aborted and never reused; an acknowledged commit still returns its saved detail and is never followed by rollback.

## Testing Requirements
Private H2 PostgreSQL-mode database with unchanged V1–V4: generated IDs, exact stored pipe text, detail decode parity, zero views, source null/empty, metadata null/empty/JSON text, UTC timestamp round-trip, and separate-connection visibility. Assert unchanged old rows after constraint failures. Use a delegating JDBC connection double to inject failures after a real insert (key retrieval/detail mapping and commit before actual commit), prove rollback leaves no row, and verify close/rollback/autocommit ordering including suppressed cleanup failures. Inject rollback failure, close failure, and autocommit-restoration failure separately on failed and acknowledged-commit paths where applicable. Verify no autocommit reset after failed rollback, physical invalidation/abort, and a subsequent pool borrow cannot reuse that connection; ordinary close invocation alone is insufficient evidence. For acknowledged commit followed by restoration/close failure, assert returned detail, separate-connection row visibility, outer-layer error logging, invalidation, and zero rollback calls. For failure paths assert the primary error retains cleanup failures as suppressed. Do not merely mock a thrown exception before any SQL. Run `./gradlew :backend:test --tests be.lutske.leolegacy.infrastructure.persistence.jdbc.JdbcRecipeWriteRepositoryTest -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing JDBC H2/Flyway test dependencies; production datasource must allow local JDBC transactions. No user database access is required for tests.
