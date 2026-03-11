# Translate seed data and remaining Dutch references to English

**Date:** 2026-03-10

## Summary
Translated all remaining Dutch content in the codebase to English:
- `V3__add_recipe_import_fields.sql`: category name "Geimporteerd" → "Imported"
- Backend Java: comment and test references updated from "Geimporteerd" to "Imported"
- Frontend test mock data: Dutch recipe titles, ingredients, category names → English
- README and release notes: "Geimporteerd" → "Imported"

Note: V2__seed.sql was already translated in a prior change. The `leo-legacy-static/` folder is legacy reference data and was intentionally left unchanged.

## User Impact
- The "Imported" category now appears with its English name in the UI.
- All seed data (categories and recipes) displays in English.

## Configuration / Environment Changes
None.

## How to Verify
1. Run `./gradlew clean build` — all backend tests pass.
2. Run `npm test` in `frontend/` — all frontend tests pass.
3. Start the app and verify category names and recipe content are in English.
