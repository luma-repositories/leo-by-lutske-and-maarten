# Replace OCR with LangChain4j LLM-based Recipe Extraction

**Date:** 2026-03-07

## Summary

Replaced the Tess4J/OCR-based recipe import pipeline with a **LangChain4j multimodal LLM** approach. Instead of OCR → text → heuristic parsing, uploaded recipe images are now sent directly to a vision-capable LLM which returns structured recipe JSON. The system supports **OpenAI**, **Claude (Anthropic)**, and **local vLLM** providers, switchable via configuration only.

## User Impact

- **Import quality**: Recipe extraction is significantly more accurate, especially for handwritten recipes, complex layouts, and non-English text. The LLM understands context, not just characters.
- **Same UI flow**: The upload → review → confirm flow remains the same. Users will notice better pre-filled fields and fewer corrections needed.
- **AI-powered**: The loading message now says "Afbeelding wordt geanalyseerd door AI..." instead of referencing OCR.
- **Provider flexibility**: Operators can switch between OpenAI, Claude, or a self-hosted vLLM endpoint without code changes.

## Configuration / Environment Changes

### New configuration properties (`application.properties`)

| Property | Description | Default |
|---|---|---|
| `app.ai.provider` | AI provider: `openai`, `claude`, or `vllm` | `openai` |
| `app.ai.model` | Model name (e.g. `gpt-4o`, `claude-sonnet-4-20250514`) | `gpt-4o` |
| `app.ai.api-key` | API key (use `${AI_API_KEY}` env var) | `dummy-key-for-dev` |
| `app.ai.base-url` | Base URL (required for `vllm` provider) | — |
| `app.ai.timeout-seconds` | Request timeout | `120` |
| `app.ai.temperature` | LLM temperature (low = more deterministic) | `0.1` |
| `app.ai.max-tokens` | Max response tokens | `4096` |

### Removed configuration

| Property | Reason |
|---|---|
| `ocr.tessdata-path` | Tess4J removed |
| `ocr.language` | Tess4J removed |

### Environment variable

Set `AI_API_KEY` to your OpenAI or Anthropic API key:
```bash
export AI_API_KEY=sk-your-key-here
```

## Platform Dependency Changes

- **Removed**: `tess4j` (Tesseract OCR Java wrapper)
- **Added**: `langchain4jOpenai` and `langchain4jAnthropic` (already declared in platform at `0.26.2`, now wired into `build.gradle.kts`)

## Database Migration

- **V4__add_import_metadata.sql**: Adds `import_metadata TEXT` column to the `recipe` table for storing AI extraction metadata (provider, model, warnings).

## How to Verify

1. Set `AI_API_KEY` environment variable
2. Start the app: `./gradlew :backend:quarkusDev`
3. Navigate to `/import`
4. Upload a recipe image
5. Verify the LLM extracts structured data
6. If incomplete, fill in missing fields and confirm
7. Verify the recipe is saved and visible in the recipe list

### Provider switching

```properties
# OpenAI
app.ai.provider=openai
app.ai.model=gpt-4o

# Claude
app.ai.provider=claude
app.ai.model=claude-sonnet-4-20250514

# Local vLLM
app.ai.provider=vllm
app.ai.model=llava-v1.6-mistral-7b
app.ai.base-url=http://localhost:8000/v1
app.ai.api-key=not-needed
```

## Files Changed

### Backend — Removed
- `OcrService.kt` — Tess4J OCR wrapper
- `RecipeParserService.kt` — Heuristic text parser
- `RecipeParserServiceTest.kt` — Parser unit tests

### Backend — New
- `RecipeExtractionService.kt` — Provider-agnostic extraction interface
- `ExtractionResult` — Extraction result data class with validation
- `ChatModelProducer.kt` — CDI producer for LangChain4j ChatLanguageModel
- `LangChain4jRecipeExtractionService.kt` — LangChain4j implementation
- `V4__add_import_metadata.sql` — Database migration
- `ExtractionResultValidationTest.kt` — Unit tests for extraction validation

### Backend — Modified
- `platform/quarkus-platform.gradle` — Removed tess4j
- `backend/build.gradle.kts` — Removed tess4j, added LangChain4j deps
- `application.properties` — AI config replaces OCR config
- `RecipeImportResource.kt` — Uses extraction service instead of OCR+parser
- `RecipeImportDtos.kt` — Updated DTOs with new fields
- `RecipeEntity.kt` — Added `importMetadata` field
- `RecipeImportResourceTest.kt` — Mocks extraction service
- `RecipeImportIntegrationTest.kt` — Updated for new flow
- `PostgresIntegrationTestProfile.kt` — Removed Tesseract config

### Frontend — New
- `locales/nl.json` — Dutch i18n strings
- `i18n/useTranslation.ts` — Simple translation hook

### Frontend — Modified
- `api/client.ts` — Updated types for new response shape
- `ImportRecipePage.tsx` — Uses i18n, updated for AI response handling
- `ImportRecipePage.test.tsx` — Updated for new response shape
- `client.test.ts` — Updated for new response shape
