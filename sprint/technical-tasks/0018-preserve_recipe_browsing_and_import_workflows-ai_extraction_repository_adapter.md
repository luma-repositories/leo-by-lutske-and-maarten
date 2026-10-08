# Title
Adapt the existing LangChain4j extraction service to the core repository port

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — English extraction, uncertainty, and mandatory review; eventual story location.

## Objective
Bridge existing provider/parsing behavior to the exact core extraction result without breaking the legacy service API.

## Scope
Add `LangChain4jRecipeExtractionRepository implements RecipeExtractionRepository`, delegating to the existing outer `RecipeExtractionService` bean.

## Out of Scope
Changing prompts/parsers, provider retries, persistence, auto-save, REST rewiring, new AI dependencies in core, and deleting delegate models.

## Clean Architecture Placement
Outer `be.lutske.leolegacy.infrastructure.ai`, `@ApplicationScoped` with constructor injection. The existing `LangChain4jRecipeExtractionService` remains the implementation of the old outer `application.service.RecipeExtractionService`; only the new adapter implements the core port.

## Execution Dependencies
- `0004-preserve_recipe_browsing_and_import_workflows-define_repository_and_confirmation_contracts.md`

## Implementation Details
- Exact public port method: domain `ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType)`. Pass validated bytes/MIME unchanged to the delegate once. No filename/filesystem or content validation here.
- Map all eleven legacy result fields explicitly in order: title, description, String servings, ingredients, steps, source, tags, warnings, rawModelResponse, provider, model. Preserve nulls, empty lists, list order, raw text, and warnings. Domain constructor only supplies its specified null-warning default; do not apply additional normalization.
- Catch the legacy `application.service.RecipeExtractionException` and throw domain `RecipeExtractionException(message, legacyException)` preserving message/cause. Unexpected failures remain failures; never manufacture an empty success result.
- Preserve the delegate's exact English-only/non-inventing prompt, uncertainty warnings, code-fence removal, blank filtering, and malformed-JSON fallback. A malformed JSON response is an incomplete result with cleaned raw text and the existing warning, whereas model-call failure throws. Do not equate these paths.
- **Return-type conflict:** the two `extractRecipeFromImage(byte[], String)` methods return unrelated legacy/domain records. Java cannot implement both interfaces in the old class with those return types. Keep the delegate/service/result/exception as outer compatibility types, and use explicit mapping in this new class. Keep existing `QuarkusMock` mocks of `LangChain4jRecipeExtractionService` effective through delegation.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/infrastructure/ai/LangChain4jRecipeExtractionRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/ai/LangChain4jRecipeExtractionRepositoryTest.java`
- `backend/src/test/java/be/lutske/leolegacy/infrastructure/ai/LangChain4jRecipeExtractionServiceTest.java`
- Retained delegates: `backend/src/main/java/be/lutske/leolegacy/infrastructure/ai/LangChain4jRecipeExtractionService.java`, `backend/src/main/java/be/lutske/leolegacy/application/service/RecipeExtractionService.java`, `backend/src/main/java/be/lutske/leolegacy/application/service/ExtractionResult.java`, `backend/src/main/java/be/lutske/leolegacy/application/service/RecipeExtractionException.java`.

## Acceptance Criteria
- Given complete/partial/empty legacy results, when adapting, then all corresponding domain fields and completeness semantics match.
- Given malformed JSON, when the real delegate parses a stub model response, then reviewable empty content/raw text/warning survives mapping.
- Given model-call failure, when adapting, then a domain extraction exception escapes and no review or saved result is manufactured.

## Testing Requirements
Plain tests with a recording legacy service fake for bytes/MIME/eleven-field fidelity and exception cause. Also construct the real delegate with a stub/mock `ChatLanguageModel` and real `ObjectMapper`: fenced JSON, malformed text, no recipe, and provider failure. Capture model request to verify English translation and non-invention instructions remain present, without live credentials. Run `./gradlew :backend:test --tests 'be.lutske.leolegacy.infrastructure.ai.LangChain4jRecipeExtraction*Test' -x :backend:buildFrontend -x :backend:copyFrontend`.

## Dependencies / Preconditions
Existing LangChain4j/Jackson test classpath; no network or AI credentials.
