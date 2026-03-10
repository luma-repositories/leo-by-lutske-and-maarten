# Translate imported recipes to English

**Date:** 2026-03-10

## Summary
The AI recipe extraction prompt now instructs the LLM to always translate all extracted content to English, regardless of the input language. Previously, the original language was preserved.

## User Impact
- Recipes imported from images in any language (Dutch, French, etc.) will now have their title, ingredients, steps, and other fields translated to English.
- User-facing fallback strings updated: "Naamloos recept" → "Untitled Recipe", "Notities:" → "Notes:".

## Changes
- `LangChain4jRecipeExtractionService.java` — Updated extraction prompt to require English translation; changed Dutch examples to English.
- `RecipeImportResource.java` — Changed "Notities: " to "Notes: " and "Naamloos recept" to "Untitled Recipe".
- `RecipeImportResourceTest.java` — Updated mock data to English; added test for "Notes: " prefix.

## Configuration / Environment Changes
None.

## How to Verify
1. Import a recipe image in a non-English language — the extracted fields should be in English.
2. Run `./gradlew clean build` — all tests pass.
