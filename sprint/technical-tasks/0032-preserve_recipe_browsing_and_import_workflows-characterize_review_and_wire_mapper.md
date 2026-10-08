# Title
Characterize editable extraction review and wire its request mapper

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Prove every successful extraction remains editable and replace only inline confirmation mapping with the pure mapper.

## Scope
Review rendering tests and a small mapper integration in the existing ImportRecipePage.

## Out of Scope
New review components, extraction retries, reset changes, save failure/retry coverage, category controls, translation generation, conversion logic, and broader frontend restructuring.

## Clean Architecture Placement
React retains the review state and rendering; the application mapper translates current edit strings into the existing adapter request. No new runtime services.

## Execution Dependencies
- `0027-preserve_recipe_browsing_and_import_workflows-extract_review_request_mapper.md`

## Implementation Details
- Input is ImportExtractionResponse; output is the existing review DOM plus a mapper-built request on explicit confirmation.
- COMPLETE uses the complete banner; NEEDS_MORE_INFO uses the incomplete banner, including proposals whose title/ingredients/preparation are null or omitted. Empty extraction is an editable review, not an upload error or automatic save.
- Preserve prefills: nullish title/preparation become empty strings; ingredients join with newlines; edit notes start empty even if proposal notes exist. All four controls remain editable in every review state.
- Render warning lists and localized missing-field labels only for nonempty arrays; preserve the existing missing-field classes. Raw source details exist only for nonempty rawModelResponse, start collapsed, can expand, and contain a read-only textarea with exact source text.
- Replace the inline ingredients/request construction in handleConfirm with `buildImportConfirmRequest(extraction, { title: editTitle, ingredients: editIngredients, preparation: editPreparation, notes: editNotes })`. Keep navigation, UI guards, and error state in the page.
- Retain all metadata and nullable category information. No category selector. English fixture content is displayed unchanged; mocked tests do not prove provider translation or non-invention.
- No confirmation request or navigation occurs on extraction alone, regardless of completeness. Server validation of remaining required content remains the confirmation boundary.

## Files / Modules Impacted
- `frontend/src/pages/ImportRecipePage.tsx`
- `frontend/src/pages/ImportRecipePage.test.tsx`
- Consume: `frontend/src/application/import/buildImportConfirmRequest.ts`.
- Reference only: `frontend/src/api/client.ts`, `frontend/src/locales/en.json`.

## Acceptance Criteria
- Given complete, incomplete, or empty extraction, when review appears, then all four edit controls are available and extraction alone never confirms or navigates.
- Given warnings/missing fields/source text, when rendered, then their existing sections show accurate content; given absent optional content, then those sections are absent.
- Given edited review values, when explicitly confirmed, then the posted request follows the mapper rules and retains proposal metadata.

## Testing Requirements
Use isolated Vitest/RTL, MemoryRouter, restored fetch/navigation spies, and local object URL stubs. Exact cases: COMPLETE prefills all controls and complete banner; NEEDS_MORE_INFO retains available content and shows exact missing labels/classes and ordered warnings; null-field and omitted-field empty proposals show editable empty fields; edit all four fields in each state; absent/empty warning and missing arrays omit sections; raw response null/empty omits details; nonempty raw details start closed, open on summary click, and retain exact read-only content; no category selector; no confirmation POST/navigation before explicit confirmation in all statuses; a completed empty proposal can submit manually supplied fields to mocked 201; inspect one actual posted mapper request with padded ingredient lines, raw response, metadata, and null proposal category. Keep detailed mapper edge cases in its unit test.

Run from `frontend`: `npm run test -- src/pages/ImportRecipePage.test.tsx src/application/import/buildImportConfirmRequest.test.ts`.

## Dependencies / Preconditions
Task 0027 supplies the mapper. Existing test infrastructure; all HTTP mocked, no backend prerequisite.
