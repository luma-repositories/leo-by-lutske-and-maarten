# Title
Delegate image extraction to ExtractRecipeUseCase with explicit DTO mapping

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — every extraction requires review; eventual story location.

## Objective
Make the extraction endpoint an upload/HTTP adapter over the pure extraction use case without any save path.

## Scope
Extraction route delegation, extraction/converter producers, and a separate `RecipeImportDtoMapper` for review output.

## Out of Scope
Confirmation migration (0023), removing legacy confirmation dependencies prematurely, prompt/parser changes, new wire fields, schema operations, and full cross-layer workflow tests (0037).

## Clean Architecture Placement
Outer REST validates/reads files and maps DTOs; outer `configuration` constructs plain core objects. The AI repository adapter implements an inward port. No REST, CDI, filesystem, or AI library references enter core.

## Execution Dependencies
- `0010-preserve_recipe_browsing_and_import_workflows-extract_recipe_use_case.md`
- `0018-preserve_recipe_browsing_and_import_workflows-ai_extraction_repository_adapter.md`
- `0019-preserve_recipe_browsing_and_import_workflows-wire_category_resource.md`
- `0021-preserve_recipe_browsing_and_import_workflows-isolate_recipe_image_upload_validation.md`

## Implementation Details
- Inject `ExtractRecipeUseCase`. For `POST /api/recipes/import`, run validator, read uploaded bytes in REST, then call `execute(byte[], effectiveMime)` exactly once. Handle I/O as existing HTTP 500 `{"error":"Failed to read uploaded file: ..."}` and domain extraction exception as HTTP 500 `{"error":"AI extraction failed: ..."}`. Bad upload remains 400. Failure is never COMPLETE or an empty successful review.
- Map `RecipeReviewResult` to existing `ImportExtractionResponse(status, rawModelResponse, proposedRecipe, missingFields, warnings)` and return 200 for COMPLETE, NEEDS_MORE_INFO, empty/no-recipe, and malformed-JSON fallback results. There is no generated ID or save operation for any status. Remove extraction's inline conversion/status assembly, leaving legacy confirmation code compiling until 0023.
- Add separate plain `interfaceadapter.rest.mapper.RecipeImportDtoMapper` with explicit `toExtractionResponse(RecipeReviewResult)` and proposal/conversion mapping helpers. Map all proposal fields title, description, String servings, ingredients, preparation, source, tags, nullable categoryId, notes, convertedIngredients. Map all six conversion fields (including boolean) explicitly. Preserve null versus empty lists/strings and ordered values; mapper does not perform conversion or validation.
- Existing `ProposedRecipeDto.convertedIngredients` uses the old outer `application.service.IngredientConversion` record. Retain that wire-facing record and construct it field-for-field from domain conversions; do not expose domain records directly as DTOs or change JSON. This compatibility record remains used after removal task 0025. Distinguish legacy/domain classes with full names where needed.
- Extend `RecipeUseCaseProducer`: one producer for plain domain `IngredientConverter`, one for `new ExtractRecipeUseCase(coreRecipeExtractionRepository, domainIngredientConverter)`. Use dependent-scoped products, reuse these producers for 0023, and leave the old outer converter wrapper available solely to unmigrated confirmation until then. No duplicate core converter producer.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeImportDtoMapper.java`
- `backend/src/main/java/be/lutske/leolegacy/configuration/RecipeUseCaseProducer.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeImportDtoMapperTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResourceTest.java`
- Retain `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportDtos.java` wire contract and `backend/src/main/java/be/lutske/leolegacy/application/service/IngredientConversion.java`.

## Acceptance Criteria
- Given any successful extraction completeness state, when POST returns, then 200 review DTOs preserve the current fields and no recipe is persisted.
- Given provider failure, when POST returns, then it is a 500 error rather than a successful incomplete review.
- Given null metadata/optional fields, when mapping, then null and empty values remain distinguishable and no category is assigned at extraction time.

## Testing Requirements
Plain mapper tests assert all fields, String servings, six conversion fields, null/empty collections, warnings/raw text. Extend existing H2 `RecipeImportResourceTest` using its old LangChain4j service mock through the new adapter: complete, partial, no-arg empty, malformed-response fallback and exception. Snapshot recipe count with JDBC before/after each extraction/error and assert unchanged; avoid coupling to earlier tests' inserted rows. Check exact conversion display and no category assignment. Run `./gradlew :backend:test --tests be.lutske.leolegacy.interfaceadapter.rest.mapper.RecipeImportDtoMapperTest --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeImportResourceTest -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing H2 test profile and deterministic mocked AI delegate; no credentials/network.
