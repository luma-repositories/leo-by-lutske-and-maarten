# Migrate Remaining Kotlin to Java 25 + Remove Tess4J/OCR

**Date**: 2026-03-10

## Summary

Migrated the last 3 Kotlin source files and 1 Kotlin test file to Java 25, completing the full Kotlin → Java migration. Removed the Tess4J/Tesseract OCR path entirely — recipe import now uses AI-powered extraction via LangChain4j (multimodal LLM). The backend is now 100% Java 25 with zero Kotlin dependencies.

## Changes

### Migrated Files (Kotlin → Java)
- `RecipeExtractionService.kt` → `RecipeExtractionService.java` (interface)
- `ExtractionResult` data class → `ExtractionResult.java` (Java record)
- `RecipeExtractionException` → `RecipeExtractionException.java`
- `LangChain4jRecipeExtractionService.kt` → `LangChain4jRecipeExtractionService.java`
- `ChatModelProducer.kt` → `ChatModelProducer.java`
- `ExtractionResultValidationTest.kt` → `ExtractionResultValidationTest.java` (12 unit tests)

### Removed Files (OCR/Tess4J)
- `OcrService.java` — Tess4J-based OCR service (replaced by AI extraction)
- `OcrException.java` — OCR exception class
- `RecipeParserService.java` — Plain-text recipe parser for OCR output
- `RecipeParserServiceTest.java` — Parser unit tests

### Rewired Import Flow
- `RecipeImportResource.java` — Now uses `RecipeExtractionService` (AI/LLM) instead of `OcrService` + `RecipeParserService`
- `RecipeImportDtos.java` — Updated DTOs: `steps` (list) replaces `preparation` (string), added AI metadata fields (provider, model, rawModelResponse)
- `RecipeImportResourceTest.java` — Mocks `LangChain4jRecipeExtractionService` instead of `OcrService`
- `RecipeImportIntegrationTest.java` — Updated for new DTO structure, removed Tesseract prerequisite

### Platform / Dependencies
- `platform/quarkus-platform.gradle`: Updated Quarkus `3.17.7` → `3.32.2`, removed Kotlin deps, removed `tess4j`
- Switched from Quarkiverse LangChain4j extensions to plain `dev.langchain4j:langchain4j-*:1.0.0-beta2` (avoids build-time processor incompatibility with Quarkus 3.32.2)
- Changed `quarkus-hibernate-orm-panache-kotlin` → `quarkus-hibernate-orm-panache` (Java Panache)

### Entity Update
- `RecipeEntity.java`: Added `importMetadata` field (TEXT column) to match V4 migration

### README
- Removed all Tesseract/OCR references
- Updated project structure to reflect actual files
- Corrected tech stack: Java 25, Quarkus 3.32.2, Gradle 9.3.1, LangChain4j 1.0.0-beta2

## User Impact

- **Import API contract changed**: `preparation` (string) → `steps` (list of strings) in request/response DTOs
- **No Tesseract installation required** — recipe import uses AI providers only
- **API key required** for recipe import (OpenAI, Anthropic, or vLLM)

## How to Verify

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.2-open
./gradlew :backend:clean :backend:build --no-daemon
```

All tests should pass. Integration tests (requiring Postgres + AI key) are excluded by default.

## Migration Notes

- If your local Postgres has stale Flyway state from a previous branch, run:
  `podman compose down -v && podman compose up -d`
- Frontend `ImportRecipePage` may need updating to use `steps` instead of `preparation` in API calls
