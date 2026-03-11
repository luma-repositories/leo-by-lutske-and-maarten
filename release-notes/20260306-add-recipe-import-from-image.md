# Add Recipe Import from Image (OCR)

## Summary
- Added a full-stack "Recipe Import from Image" feature that uses Tesseract OCR (via Tess4J) to extract text from uploaded recipe photos, parse it into structured recipe data, and either save it directly or present a review form for the user to fill in missing fields.
- Backend: two new REST endpoints (`POST /api/recipes/import` for multipart upload, `POST /api/recipes/import/confirm` for user-corrected data), plus `OcrService`, `RecipeParserService`, and supporting DTOs.
- Frontend: new `ImportRecipePage` with image upload, preview, loading state, OCR review form (raw text + editable fields), and confirm/cancel flow. "Importeer" button added to the Header.
- New "Imported" category (id=15) for imported recipes.
- Database migration `V3__add_recipe_import_fields.sql` adds `source` and `created_at` columns to the recipe table.

## User Impact
- Users can now navigate to `/import` (or click "Importeer" in the header) to upload a recipe photo.
- If OCR fully parses the recipe (title + ingredients + preparation), it is saved immediately and the user is redirected to the recipe detail page.
- If fields are missing, the user sees the raw OCR text alongside an editable form pre-filled with detected values, and can correct/complete the recipe before saving.
- All UI strings are in Dutch, consistent with the rest of the application.

## Config/Env Changes
- `ocr.tessdata-path` — path to Tesseract tessdata directory (default: `/opt/homebrew/share/tessdata`)
- `ocr.language` — OCR language code (default: `nld` for Dutch)
- `quarkus.http.limits.max-body-size` — set to `10M` to allow image uploads
- **Prerequisite**: Tesseract OCR must be installed locally (`brew install tesseract` on macOS)

## Platform Dependency Notes
- `platform/quarkus-platform.gradle`: added `tess4j: '5.13.0'`, `mockitoKotlin: '5.4.0'`, `quarkusMockito` library entry
- `backend/build.gradle.kts`: added `tess4j`, `quarkus-junit5-mockito`, `mockito-kotlin` dependencies

## Migration Notes
- `V3__add_recipe_import_fields.sql` adds two nullable columns (`source`, `created_at`) to the `recipe` table — no data migration needed.
- Inserts the "Imported" category with id=15.
- Resets identity sequences to avoid conflicts with seed data.

## Test Coverage Mapping
| Component/Module | Tests | What's Covered |
|---|---|---|
| `RecipeParserService` (backend) | 10 unit tests | Structured recipes, Dutch headings, ALL CAPS titles, heuristic parsing, empty/blank text, partial recipes, heading synonyms |
| `RecipeImportResource` (backend) | 6 integration tests | Full parse → 201, incomplete → 422, confirm → 201, user overrides, missing title → 400, empty OCR → 422 |
| `ImportRecipePage` (frontend) | 14 component tests | Page rendering, e2e IDs, upload button states, file selection, loading state, 201 redirect, 422 review form, error display, confirm flow, empty title validation, confirm failure, cancel/reset |
| `Header` (frontend) | 1 new test | Importeer button rendering, link to /import, CSS class |
| `api/client.ts` (frontend) | 6 new tests | importRecipeImage (201, 422, error, JSON parse failure), confirmRecipeImport (success, error) |

## How to Verify
1. **Prerequisites**: Install Tesseract OCR (`brew install tesseract` on macOS)
2. **Backend**: `./gradlew :backend:build` — all backend tests pass (30 tests)
3. **Frontend tests**: `cd frontend && npm test` — all frontend tests pass
4. **Frontend build**: `cd frontend && npm run build` — TypeScript + Vite build succeeds
5. **Manual test**: Start the app, click "Importeer" in the header, upload a recipe photo, verify OCR extraction and review form
