# Title
Verify cancellation and image reset clear the import session

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Protect the existing shared reset handler against stale images, errors, proposals, and corrections.

## Scope
Cancellation/reset tests and only a local cleanup correction if those tests expose a gap.

## Out of Scope
New state stores/hooks/components, import architecture redesign, request abort/retry behavior, new confirmation flows, and changes to disabled controls during pending requests.

## Clean Architecture Placement
Session reset and object URL ownership remain local presentation/browser lifecycle concerns in ImportRecipePage.

## Execution Dependencies
None.

## Implementation Details
- Inputs: Choose Again in selection, or Cancel in settled review. Output: empty selection mode, no preview, no proposal, blank corrections, cleared error and native file input value.
- Current handleReset already clears selectedFile, previewUrl, extraction, all four edits, error, and the mounted file input's value. Characterize before changing it; review cancellation remounts a fresh file input because the upload section was hidden.
- Exercise cancellation after editing every field and after a mocked failed confirmation, so stale error cleanup is observable. Record request counts before cancellation and assert cancellation itself sends nothing and never navigates.
- Start another extraction with a different proposal and then an empty proposal to expose leaked title/ingredients/preparation/notes, warning/source content, or proposal metadata. Re-selecting the same File after reset must work.
- If adding object URL revocation, use a small local cleanup for replacement/reset/unmount and keep the active preview valid until replaced or cleared. This resource cleanup is permitted but is not a reason for a state architecture change.
- Preserve existing disabled reset during extraction and disabled cancel during confirmation; this task adds no in-flight cancellation semantics or new validation/errors.

## Files / Modules Impacted
- `frontend/src/pages/ImportRecipePage.test.tsx`
- `frontend/src/pages/ImportRecipePage.tsx` only if cleanup needs a minimal fix.

## Acceptance Criteria
- Given a selected image, when Choose Again is clicked, then preview and native input selection clear and extraction becomes unavailable.
- Given edited review with an error, when Cancel is clicked, then empty image selection returns, the error clears, and cancellation causes no save/navigation.
- Given a cancelled session, when another image is extracted, then only the new proposal and new edits are present.

## Testing Requirements
Isolated Vitest/RTL with restored fetch/navigation spies, local createObjectURL/revokeObjectURL stubs, and fresh router mounts. Exact cases: selection reset removes preview, clears input value/files, disables upload, removes reset button, and makes no request; reset after extraction HTTP error clears alert; same-file selection after reset previews again; edit all fields then cancel removes review/status/warnings/raw source and clears image/input; failed confirmation then cancel clears save error; next distinct and empty extraction contain no previous corrections or notes; cancelled workflow never adds a confirm request (distinguish a preceding deliberately failed save). If revocation is implemented, test replacement, reset/cancel, and unmount release obsolete URLs without revoking the active preview early. Keep assertions observable rather than inspecting React state.

Run from `frontend`: `npm run test -- src/pages/ImportRecipePage.test.tsx`.

## Dependencies / Preconditions
Existing reset and confirmation catch behavior support independent tests. No earlier task artifacts or backend prerequisites are required.
