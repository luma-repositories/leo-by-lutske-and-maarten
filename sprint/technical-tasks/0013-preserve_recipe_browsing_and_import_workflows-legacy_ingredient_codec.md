# Title
Preserve the legacy pipe-separated ingredient representation

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — existing ingredients and their order; eventual story location.

## Objective
Give JDBC reads and writes one storage codec matching the inspected REST/entity representation.

## Scope
Add a small stateless `LegacyIngredientCodec` with `List<String> decode(String stored)` and `String encode(List<String> ingredients)`.

## Out of Scope
Conversion, review validation, escaping pipes, JSON storage, schema/data migrations, database operations, REST wiring, and ORM functionality.

## Clean Architecture Placement
`be.lutske.leolegacy.infrastructure.persistence.jdbc` in outer `:backend`. Storage encoding is not domain behavior; production core remains JDK-only. The codec can be a plain final class with static methods.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
- `decode` reproduces `Arrays.stream(stored.split("\\|")) .filter(s -> !s.isBlank()).toList()` from both current resources. Do not trim surviving values, reorder, reconvert metric text, or normalize preparation.
- `encode` uses exactly `String.join("|", ingredients)`; write ordered display strings from `NewRecipe`, not conversion metadata. Do not filter or trim on write.
- `recipe.ingredients` is `TEXT NOT NULL` in V1. Inputs are non-null stored strings/non-null ingredient lists; null is a programming/contract error, not an empty historical value. No new element validation or escaping protocol. Preserve Java join behavior rather than inventing a new format.
- Empty stored string and only delimiters/blank segments decode to an empty list. Embedded pipes remain delimiters; the legacy format cannot round-trip an ingredient containing a literal pipe as a single entry. Do not silently change that representation.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/LegacyIngredientCodec.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/jdbc/LegacyIngredientCodecTest.java`

## Acceptance Criteria
- Given `"  flour| |4 eggs ||"`, when decoded, then the result is exactly `["  flour", "4 eggs "]`.
- Given ordered `["200 g flour", " 4 eggs "]`, when encoded, then storage is exactly `"200 g flour| 4 eggs "`.
- Given empty storage or only blank segments, when decoded, then an empty list is returned without writes.

## Testing Requirements
Plain JUnit: empty string/list, leading/trailing/repeated pipes, whitespace-only segments, preserved whitespace/newlines in surviving ingredients, metric text, and literal-pipe legacy behavior. Run `./gradlew :backend:test --tests be.lutske.leolegacy.infrastructure.persistence.jdbc.LegacyIngredientCodecTest -x :backend:buildFrontend -x :backend:copyFrontend`. No database or Quarkus boot required.

## Dependencies / Preconditions
JDK 25 and existing Gradle dependencies.
