# Always English + Always Review Form

**Date:** 2026-03-09

## Summary

Two behavioral requirements are now enforced in the recipe import pipeline:

1. **Always translate to English**: Regardless of the input language of the recipe image (Dutch, Italian, French, etc.), the LLM extraction prompt instructs the model to translate all extracted content — title, ingredients, steps, description — into English.

2. **Never auto-save**: After uploading an image, the user always sees an editable review form with the extracted data. The `POST /api/recipes/import` endpoint always returns HTTP 200 with a `status` of `COMPLETE` or `NEEDS_MORE_INFO`. The user must explicitly confirm (via `POST /api/recipes/import/confirm`) before the recipe is saved to the database.

## User Impact

- Imported recipes will always have English text, making the recipe catalog consistent.
- Users always get a chance to review and correct extracted data before saving.
- Error cases (e.g., "not a recipe") are shown in the review form with warnings rather than as opaque error pages.

## How to Verify

1. Upload a recipe image in any language (e.g., Dutch or Italian).
2. Confirm the review form shows English-translated content.
3. Confirm the recipe is **not** saved until you click the confirm button.
4. Upload a non-recipe image and confirm a warning is shown in the review form.
