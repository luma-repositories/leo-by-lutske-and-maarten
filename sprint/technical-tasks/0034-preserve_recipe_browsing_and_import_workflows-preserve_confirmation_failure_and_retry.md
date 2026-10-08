# Title
Preserve confirmation corrections, failure recovery, and success navigation

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Prove confirmation failures retain editable corrections for explicit retry and navigation happens only after saved-recipe success.

## Scope
Confirmation-focused page tests and minimal local guards/fixes needed to pass them.

## Out of Scope
Automatic retries, backend save/conversion implementation, category selection, new components, broad validation, extraction retry changes, and architecture migration.

## Clean Architecture Placement
The existing page owns pending/error/navigation state, the mapper owns request construction, and the existing HTTP adapter determines 201 success. Required-field fallback, conversion, notes composition, and default category resolution stay server-owned.

## Execution Dependencies
- `0028-preserve_recipe_browsing_and_import_workflows-preserve_api_adapter_contracts.md`
- `0032-preserve_recipe_browsing_and_import_workflows-characterize_review_and_wire_mapper.md`

## Implementation Details
- Inputs: current extraction and all four correction strings; output: mapper request followed by retained review/error or navigation to `/recipes/${recipe.id}`.
- Keep `confirming || !editTitle.trim()` as the disabled predicate. Add only a minimal matching handler guard for absent extraction, pending confirmation, and blank/whitespace-only title if needed; repeated pending activation must submit once.
- Preserve entered corrections on HTTP failure, unexpected successful status, rejected fetch, or invalid success JSON. Display existing `Save failed. Please try again.`, release confirming in finally, remain in review, and allow an explicit retry with the latest edits. Clear the previous error when the next attempt begins.
- While pending, preserve `Saving...` and disabled confirm/cancel. Navigate once only after a 201 recipe response; never invent a recipe ID or successful-save state.
- Inspect request payloads: trimmed/nonempty ingredient lines in order; empty ingredients/preparation become null overrides with original proposed values intact for fallback; raw response and proposal metadata survive. If proposal also lacks required values, mock server validation failure and retain review.
- Keep proposal categoryId null in the ordinary import fixture and category override absent (null-equivalent), allowing the server's default Imported category. Do not hard-code its numeric ID or introduce a selector. Mock a missing-default-category save failure too.
- Nonempty notes are passed separately, not concatenated by the browser. Use a 201 Imported fixture with `7.1 oz (200 g) dark chocolate`, `4 eggs`, and `Mix.\n\nNotes: Serve chilled.` to represent the server contract; already converted, imperial, and vague quantities pass through without frontend conversion. Mocked fixtures do not prove backend conversion/persistence.
- Remove imports left unused by the page/mapper wiring changes. Keep existing TypeScript strictness and unused-symbol checks; do not weaken tsconfig, suppress diagnostics, or bypass type checking to make the build pass.

## Files / Modules Impacted
- `frontend/src/pages/ImportRecipePage.test.tsx`
- `frontend/src/pages/ImportRecipePage.tsx` only for minimal confirmation guards/state fixes.
- Consume: `frontend/src/application/import/buildImportConfirmRequest.ts`, `frontend/src/api/client.ts`.
- Reference only: `frontend/src/locales/en.json`.

## Acceptance Criteria
- Given empty or whitespace-only title, when confirmation is attempted, then the action is unavailable and no confirmation request occurs.
- Given corrected review and a failed save, when the request settles, then all corrections and proposal remain, the failure message appears, and no navigation occurs.
- Given retained review after failure, when the user edits and explicitly retries successfully, then the latest corrections are sent and navigation occurs once to the returned ID.
- Given cleared ingredients/preparation, when confirmed, then null overrides retain the proposal fallback; absent required proposal content yields a mocked failure with editable review.
- Given a null category proposal, when confirmed, then no category selection/override is introduced and a successful Imported response can navigate.

## Testing Requirements
Use existing Vitest/RTL test file, real adapter/mapper, endpoint-scoped fetch mocks, local restored object URL stubs, reset navigation spies, and settled deferred promises. Exact cases: empty title and spaces-only title disabled/no POST, valid title re-enables; pending save text/disabled cancel/confirm and repeated click submits once; 400 required-content rejection, 500 save failure, missing Imported category failure, rejected fetch, malformed 201 JSON, and unexpected 200 response all retain four exact edits and show the existing alert without navigation; explicit failure-then-201 retry clears the alert while pending, uses latest edited values, and navigates exactly once; no automatic retry; request contains trimmed lines, separate notes, unchanged proposal metadata/raw source, null proposal category and no category override; cleared ingredients/preparation send null and complete-proposal mocked 201 succeeds; same clearing with absent proposed content fails; blank notes map to null; already-converted/imperial/vague ingredient lines remain unchanged; successful mocked Imported recipe with converted content/notes opens its returned route. Update the relevant success fixture from the historical `Geimporteerd` category label to story-contract `Imported` without changing product locales.

Run from `frontend`: `npm run test -- src/pages/ImportRecipePage.test.tsx src/api/client.test.ts src/application/import/buildImportConfirmRequest.test.ts`.

Final frontend gate from `frontend`: run the full `npm run test` and then `npm run build`; both must pass. The production build must exercise the existing TypeScript checks and bundling, not just the focused Vitest files.

## Dependencies / Preconditions
Task 0028 provides the strict status contract; task 0032 provides mapper wiring (and transitively task 0027). Backend responses are mocked; no backend task is a prerequisite.
