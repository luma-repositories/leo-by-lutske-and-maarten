# Add End-to-End Integration Test for Recipe Import from Image

## Summary
- Added a full end-to-end integration test (`RecipeImportIntegrationTest`) that exercises the recipe import flow against real PostgreSQL and real Tesseract OCR — no mocks.
- The test uploads the actual `test-recipe.jpg` (a handwritten Italian recipe for "Scaloppine alla pizzaiola"), runs OCR, confirms the import with user overrides, and validates the recipe is correctly persisted in the database.
- Introduced `PostgresIntegrationTestProfile` — a Quarkus test profile that switches from H2 to the real PostgreSQL database (via podman-compose) and configures the real Tesseract tessdata path.
- Added `jna.library.path=/opt/homebrew/lib` system property to the Gradle test task so Tess4J can find the native Tesseract library on macOS.

## User Impact
- No user-facing changes — this is a test-only addition.

## Config/Env Changes
- `backend/build.gradle.kts`: added `jna.library.path` system property for test tasks (required for Tess4J to find native Tesseract on macOS Homebrew).
- New `PostgresIntegrationTestProfile` overrides datasource to PostgreSQL + enables Flyway clean-at-start for test isolation.

## Test Coverage Mapping
| Test | What's Covered |
|---|---|
| `full import flow - upload handwritten image and verify recipe is persisted in database` | Real OCR on handwritten image → handles 201 or 422 → confirm with overrides → validates all recipe fields (title, 7 ingredients, preparation, category, viewCount) → fetches from DB via GET endpoint to verify persistence |
| `upload real image produces OCR output without server error` | Verifies OCR doesn't crash on real handwritten image, validates response structure (rawText, missingFields, proposedRecipe for 422; or title/ingredients for 201) |
| `imported recipe appears in Geimporteerd category listing` | Confirms recipe via import/confirm → verifies it appears in GET /api/recipes?categoryId=15 listing |

## Prerequisites
- `podman compose up -d` must be running (PostgreSQL)
- Tesseract must be installed (`brew install tesseract` on macOS)

## How to Verify
1. Start PostgreSQL: `podman compose up -d`
2. Run integration test only: `./gradlew :backend:test --tests "be.lutske.leolegacy.interfaceadapter.rest.RecipeImportIntegrationTest"`
3. Run all backend tests: `./gradlew :backend:test` — all 33 tests pass (30 existing + 3 new)
4. Full build: `./gradlew :backend:build` — BUILD SUCCESSFUL
