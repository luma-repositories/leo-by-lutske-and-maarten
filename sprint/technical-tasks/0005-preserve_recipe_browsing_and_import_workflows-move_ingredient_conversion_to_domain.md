# Title
Move the existing ingredient conversion algorithm into pure domain

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — requirement 8 and conversion scenario; eventual story path.

## Objective
Make the unchanged conversion algorithm available to core while preserving the running backend's old CDI API.

## Scope
Pure domain converter, focused characterization tests, and a temporary delegating backend wrapper with field-for-field mapping to its old conversion record.

## Out of Scope
New units, regex corrections, altered rounding, parsing improvements, REST rewiring, deleting legacy records, schema/UI changes, and metadata reconstruction for already-converted strings.

## Clean Architecture Placement
`be.lutske.leolegacy.domain.IngredientConverter` in `:core:domain` is a plain stateless public class. The old `be.lutske.leolegacy.application.service.IngredientConverter` stays in outer backend with `@ApplicationScoped`. Core never depends on the wrapper.

## Execution Dependencies
- `0003-preserve_recipe_browsing_and_import_workflows-define_import_extraction_models.md`

## Implementation Details
Domain public API:

```java
public IngredientConverter();
public IngredientConversion convert(String ingredientLine);
public List<IngredientConversion> convertAll(List<String> ingredients);
public String convertToEnglishUnits(String ingredientLine);
```

Move the algorithm verbatim from `backend/src/main/java/be/lutske/leolegacy/application/service/IngredientConverter.java`, changing only package/annotation/type references. Preserve all three regexes, regex flags, capture groups (ingredient name is group 4), the `Unit.from` switch and its existing mismatches with accepted regex spellings, and the order of vague/imperial detection before metric parsing. Do not expand recognized quantities or fix legacy spelling gaps.

Preserve constants `0.035274` (g→oz), `2.20462` (kg→lb), `0.00422675` (ml→cups), `4.22675` (liter→cups). Parsing uses double and decimal-comma replacement; formatting uses `BigDecimal.valueOf`, `HALF_UP`, trailing-zero stripping and plain strings. Weight scale is 1; cups scale is 2 below one cup, otherwise 1. Metric labels remain `g`, `kg`, `ml`, `liter`; display is converted quantity followed by parenthesized original quantity and ingredient name. The original numeric text, including decimal comma, remains in the original quantity.

Null/blank input returns `(null, null, null, null, "", false)`; nonmetric/vague/imperial input is trimmed and returned unconverted with the trimmed line as ingredientName/displayValue. Empty metric ingredient name becomes null. Null list returns empty list; `convertAll` retains list order and one result per entry, including blank entries. `convertToEnglishUnits` returns `convert(...).displayValue()`.

The old wrapper retains its public no-arg constructor and all three old method signatures returning the old service `IngredientConversion`. It owns a plain domain delegate, and maps each of the six fields into the old record. It performs no algorithm itself and introduces no CDI requirement for the domain class. Existing `RecipeImportResource`/DTOs and service tests continue compiling against old types.

## Files / Modules Impacted
- `core/domain/src/main/java/be/lutske/leolegacy/domain/IngredientConverter.java`
- `core/domain/src/test/java/be/lutske/leolegacy/domain/IngredientConverterTest.java`
- `backend/src/main/java/be/lutske/leolegacy/application/service/IngredientConverter.java`
- `backend/src/test/java/be/lutske/leolegacy/application/service/IngredientConverterTest.java`

## Acceptance Criteria
- Given `200 g dark chocolate`, when converted, then display is exactly `7.1 oz (200 g) dark chocolate` with the original metric quantity retained.
- Given an already-converted display, when converted again, then display is unchanged (metadata may now say unconverted, matching the old algorithm).
- Given an existing backend caller, when the wrapper delegates, then its old return types and all field values are preserved.

## Testing Requirements
Use plain JUnit. Retain existing fixtures `200 g sugar`, `1 kg potatoes`, `500 ml milk`, `1 liter stock`, `q.b. sale`, `2 cups flour`, and ordered lists. Add exact expectations for `100 ml milk` → `0.42 cups (100 ml) milk`, `1,5 kg potatoes` → `3.3 lb (1,5 kg) potatoes`, `4 eggs`, `salt to taste`, null/blank, uppercase `200 G sugar`, no ingredient name `200 g`, and legacy unrecognized `200 grammo sugar`/`1 litre milk` (unchanged). Assert all six conversion fields, null list handling, list order, and `convert(convert(x).displayValue()).displayValue()` equality. Add wrapper/domain field parity checks in the existing plain backend test. Run `./gradlew :core:domain:test --tests be.lutske.leolegacy.domain.IngredientConverterTest` and `./gradlew :backend:test --tests be.lutske.leolegacy.application.service.IngredientConverterTest -x :backend:buildFrontend -x :backend:copyFrontend`. No Quarkus tests in core.

## Dependencies / Preconditions
None.
