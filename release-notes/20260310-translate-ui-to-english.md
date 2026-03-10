# Translate UI to English

**Date:** 2026-03-10

## Summary
All user-facing Dutch (NL) labels and content in the frontend have been translated to English. The locale file has been renamed from `nl.json` to `en.json`, and the HTML lang attribute changed from `nl` to `en`.

## User Impact
- All UI labels, headings, buttons, loading states, and error messages are now in English.
- No functional changes — only language/text changes.

## Changes
- `frontend/index.html` — `lang="en"`, title "Leo Legacy Recipes"
- `frontend/src/locales/nl.json` → `en.json` — Dutch strings translated to English
- `frontend/src/i18n/useTranslation.ts` — imports `en.json`
- `frontend/src/components/Header.tsx` — "Recipes", "Import", "unknown"
- `frontend/src/components/Footer.tsx` — "a cooking site for food lovers"
- `frontend/src/components/CategorySidebar.tsx` — "Recipe Book", "All Recipes"
- `frontend/src/pages/HomePage.tsx` — "Most Popular Recipes", "Loading...", "No recipes found.", "views"
- `frontend/src/pages/RecipeDetailPage.tsx` — "Loading...", "Recipe not found.", "Back to", "Ingredients", "Preparation"
- All corresponding test files updated to match English strings

## Configuration / Environment Changes
None.

## How to Verify
1. Start the frontend and confirm all labels are in English.
2. Run `npm test` in the `frontend/` directory — all tests should pass.
