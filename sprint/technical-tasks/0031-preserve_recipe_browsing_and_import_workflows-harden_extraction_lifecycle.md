# Title
Test image selection and close extraction promise handling gaps

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Preserve image selection/progress behavior and recover visibly from rejected extraction promises without leaving the page loading.

## Scope
Lifecycle tests plus minimal local changes to `handleUpload`: duplicate guard and try/catch/finally.

## Out of Scope
Broader retry behavior, automatic retries, cancellation controllers, client format/size validation, review rendering expansion, confirmation handling, and new components.

## Clean Architecture Placement
Browser selection and pending/error state stay in the existing React page; HTTP contracts stay in the existing client adapter.

## Execution Dependencies
None.

## Implementation Details
- Inputs: selected File and extraction promise/result. Outputs: preview, disabled controls, progress, proposal transition, or alert.
- Preserve input accept values `image/png,image/jpeg,image/jpg,image/webp`, existing selectors, and locale labels. Selection displays its object URL; no file means no request.
- Guard `handleUpload` against no selection and an in-flight submission. Retain upload/reset disabled while loading; repeated user activation during the pending request must cause one request only.
- Wrap the awaited call and existing result branches in try/catch/finally. Preserve server result messages. On rejection, show the existing adapter-compatible fallback `Unknown error`; always release loading in finally. Avoid raw exception disclosure and new locale/style changes.
- Success pre-fills review without confirmation or navigation. Error/rejection leaves selection/preview available, hides progress, restores existing controls, and does not show review or successful-save outcomes. Do not introduce new retry policies.
- Server mock cases own supported PNG/JPG/JPEG/WEBP and inclusive 10 MiB (`10 * 1024 * 1024`, current “10 MB”). Bypass userEvent's accept filtering only in unsupported-file tests so the mock server rejection is actually exercised.

## Files / Modules Impacted
- `frontend/src/pages/ImportRecipePage.tsx`
- `frontend/src/pages/ImportRecipePage.test.tsx`
- Reference only: `frontend/src/api/client.ts`, `frontend/src/locales/en.json`.

## Acceptance Criteria
- Given no image, when extraction is attempted, then the control is disabled and no request occurs.
- Given a selected image and pending extraction, when activated again, then progress remains visible and only one extraction is submitted.
- Given server rejection or a rejected promise, when settled, then an alert appears, progress ends, and no review/save/navigation occurs.
- Given a supported image exactly at the limit and a mocked successful response, when extracted, then review appears without an additional client rejection.

## Testing Requirements
Extend existing Vitest/RTL tests; locally stub and restore createObjectURL (setup.ts does not provide it). Use endpoint-scoped fetch mocks and settled deferred promises, not permanently pending test promises. Exact cases: no-file guard; preview src/alt and enabled upload; table-driven PNG/JPG/JPEG/WEBP selection; deferred progress text `Recognizing...` and loading indicator; disabled reset/upload and repeated activation yields one request; success stops progress and shows review without confirm POST/navigation; server 502 text; unsupported GIF mocked 400 explanatory alert; supported 10 MiB + 1 byte mocked 400 alert; exactly 10 MiB mocked 200 accepted; network rejection and malformed successful JSON both show `Unknown error`, end loading, and retain selected preview. No real network or AI.

Run from `frontend`: `npm run test -- src/pages/ImportRecipePage.test.tsx`.

## Dependencies / Preconditions
Existing extraction adapter and Vitest/RTL. Tests use existing adapter behavior; task 0028 and backend work are not prerequisites.
