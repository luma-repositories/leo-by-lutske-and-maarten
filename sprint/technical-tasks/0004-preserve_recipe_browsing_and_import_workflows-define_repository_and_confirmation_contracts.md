# Title
Define small repository ports and confirmation contracts

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — browsing and reviewed persistence boundaries; eventual story path.

## Objective
Fix adapter-facing query, extraction, and save contracts before implementing use cases.

## Scope
Four usecases-package repository interfaces, confirmation command/output records, and a domain new-recipe record.

## Out of Scope
JDBC/ORM implementations, database migrations, transactions in core, generic CRUD interfaces, HTTP mappings, AI integrations, and application rewiring.

## Clean Architecture Placement
Ports and command/output are public top-level types in `be.lutske.leolegacy.usecases`, module `:core:usecases`. `NewRecipe` is in `be.lutske.leolegacy.domain`, module `:core:domain`. Dependencies point inward. Later JDBC and AI adapters implement these ports; outer Quarkus composition supplies instances.

## Execution Dependencies
- `0002-preserve_recipe_browsing_and_import_workflows-define_recipe_category_read_models.md`
- `0003-preserve_recipe_browsing_and_import_workflows-define_import_extraction_models.md`

## Implementation Details
Define the exact public interfaces and records below (all referenced model types are domain types; collections use `java.util`):

```java
interface CategoryRepository {
    List<CategoryWithCount> listWithCounts();
    Optional<Category> findById(long categoryId);
}
interface RecipeReadRepository {
    List<RecipeSummary> listAll();
    List<RecipeSummary> findByCategoryId(long categoryId);
    List<RecipeSummary> findTopByViewCount(int limit);
    Optional<RecipeDetail> findById(long recipeId);
}
interface RecipeWriteRepository {
    RecipeDetail save(NewRecipe recipe);
}
interface RecipeExtractionRepository {
    ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType);
}
record ConfirmRecipeImportCommand(String rawModelResponse,
    ProposedRecipe proposedRecipe, UserOverrides userOverrides) {}
record ConfirmRecipeImportOutput(RecipeDetail recipe) {}
// Domain package:
record NewRecipe(String title, List<String> ingredients, String preparation,
    long categoryId, int viewCount, String source, Instant createdAt,
    String importMetadata) {}
```

`Instant` is `java.time.Instant`. `NewRecipe` has no generated ID, category name, description, servings, tags, or separate notes column. These latter proposal fields are not newly persisted. Preserve source and import metadata as nullable Strings; confirmation supplies defaults where specified later. Ingredients are ordered display strings, not a pipe-encoded storage value. Storage encoding belongs to the later adapter. Prefer a defensive copy of non-null `NewRecipe.ingredients` without trimming/filtering/reordering.

Port contracts:
- `listWithCounts()` includes empty categories with count zero and returns alphabetical name order with accurate long counts. The adapter owns the ordered/count query (later JDBC can aggregate without per-category calls); core must not fetch recipe entities to count them.
- Recipe list methods return non-null lists; no new ordering for `listAll` or category lists. Unknown/empty category returns an empty list. Do not validate category existence first.
- `findTopByViewCount(limit)` returns at most the positive requested limit, ordered by view count descending; equal counts have no specified secondary order. It is an explicit query for later database-side ordering/limiting, not a fetch-all instruction.
- Both lookups return `Optional.empty()` for absence, never null. All read methods are read-only and do not increment views.
- `save` creates one recipe and returns its persisted `RecipeDetail` with generated ID and resolved category name. A failed persistence/transaction must throw, never return a fabricated detail. Later adapters/composition own transaction completion so HTTP success cannot escape on commit failure. No ORM types or transaction annotations in the port.
- Extraction receives already validated in-memory bytes and effective MIME. Later adapter retains English-only, non-inventing prompt and existing parsing/warning behavior, and translates provider failures into domain `RecipeExtractionException`.
- A command requires a proposal; `userOverrides` and `rawModelResponse` may be null. Output contains only a successfully saved detail, no status flag or review response.

## Files / Modules Impacted
- `core/domain/src/main/java/be/lutske/leolegacy/domain/NewRecipe.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/CategoryRepository.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/RecipeReadRepository.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/RecipeWriteRepository.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/RecipeExtractionRepository.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ConfirmRecipeImportCommand.java`
- `core/usecases/src/main/java/be/lutske/leolegacy/usecases/ConfirmRecipeImportOutput.java`
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/RepositoryContractsTest.java`

## Acceptance Criteria
- Given hand-written in-memory implementations, when compiled against the ports, then all signatures use only JDK/domain types.
- Given a new recipe without an ID, when a fake save succeeds, then output can carry the returned generated detail; a thrown failure yields no output.
- Given an absent lookup, when exercising a fake, then absence is represented by Optional, not a framework exception.

## Testing Requirements
Ordinary JUnit contract fixtures implement all four interfaces without frameworks: category 15 with zero count, missing recipe Optional, ordered `["flour", "eggs"]`, fixed `Instant.parse("2026-01-01T12:00:00Z")`, nullable metadata, and successful detail ID 101. Verify record field transport, null-vs-empty overrides, generated detail transport, and extraction exception transport. These compile/transport tests do not prove a future database query; downstream adapters must reuse the documented ordering/count contracts in adapter tests. Run `./gradlew :core:usecases:test --tests be.lutske.leolegacy.usecases.RepositoryContractsTest`. No Quarkus tests.

## Dependencies / Preconditions
None.
