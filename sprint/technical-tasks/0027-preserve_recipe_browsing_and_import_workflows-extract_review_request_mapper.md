# Title
Extract the pure review confirmation request mapper

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Isolate the existing request construction from `ImportRecipePage.handleConfirm` in one pure, independently tested function.

## Scope
Add the mapper and its unit tests only. Wiring the existing page is task 0032.

## Out of Scope
React restructuring, new components, category selection, transport, saving, validation services, metric conversion, notes concatenation, and page title-button guards.

## Clean Architecture Placement
One application-layer mapping function with type-only imports of the existing API DTOs. No React, browser, fetch, or router dependencies; no DTO architecture migration.

## Execution Dependencies
None.

## Implementation Details
- Export `buildImportConfirmRequest(extraction, edits): ImportConfirmRequest`, using `ImportExtractionResponse` and an edits object with string fields `title`, `ingredients`, `preparation`, and `notes`.
- Copy `rawModelResponse` and the entire `proposedRecipe` unchanged, including description, servings, categoryId, notes, source, and tags. Do not mutate either input or selectively rebuild proposal metadata.
- Split ingredient text on `\n`, trim each entry, discard empty entries, preserve order, and output `null` when the resulting list is empty. CRLF input is handled by entry trimming.
- Preserve the current `value || null` rules for title, preparation, and notes: empty strings become null; nonempty strings, including whitespace-only strings, remain unchanged. The separate UI guard disables whitespace-only titles in task 0034.
- Do not add `userOverrides.categoryId`: retain its existing absence (null-equivalent server default), including when the proposal carries null categoryId. No category lookup or hard-coded Imported ID.
- Null overrides request the existing server proposal fallback; the mapper does not merge fallback values, enforce required fields, convert quantities, or append notes. It has no error side effects and accepts typed inputs without throwing for missing proposed content.

## Files / Modules Impacted
- Add `frontend/src/application/import/buildImportConfirmRequest.ts`.
- Add `frontend/src/application/import/buildImportConfirmRequest.test.ts`.
- Reference only: `frontend/src/api/client.ts`, `frontend/src/pages/ImportRecipePage.tsx`.

## Acceptance Criteria
- Given padded ingredient lines and blank lines, when mapped, then trimmed nonempty entries retain their order.
- Given empty edits, when mapped, then all four overrides are null and the proposal remains intact for fallback.
- Given proposal metadata and already-converted ingredients, when mapped, then neither metadata nor ingredient content is changed or converted again.
- Given a whitespace-only title, when mapped, then it is preserved; title availability remains a UI responsibility.

## Testing Requirements
Use pure Vitest tests with no rendered UI or network. Exact cases: ordinary corrections; `" 200 g dark chocolate \n\n 4 eggs \r\n"` to two trimmed entries; entirely blank ingredient lines to null; empty strings to null; whitespace-only title/preparation/notes preserved; preparation newlines and notes preserved verbatim; absent/null raw response preserved; every proposal metadata field retained; omitted/null proposal fields accepted; frozen inputs not mutated; no category override; `7.1 oz (200 g) dark chocolate`, `4 eggs`, and `salt to taste` unchanged.

Run from `frontend`: `npm run test -- src/application/import/buildImportConfirmRequest.test.ts`.

## Dependencies / Preconditions
Existing TypeScript DTOs and Vitest installation. No backend prerequisite.
