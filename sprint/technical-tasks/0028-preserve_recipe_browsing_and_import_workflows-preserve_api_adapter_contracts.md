# Title
Lock down import API adapter success and failure contracts

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Preserve the existing browser transport contract and recognize confirmation success only for HTTP 201.

## Scope
Extend client tests and make the narrowly required confirmation status check. Preserve existing extraction result/error handling.

## Out of Scope
UI state, backend implementation, new API abstraction layers, client file validation, automatic retries, and runtime DTO schema frameworks.

## Clean Architecture Placement
The existing `api/client.ts` is the HTTP adapter. Keep transport and status interpretation there without moving page state or domain transformations into it.

## Execution Dependencies
None.

## Implementation Details
- Input extraction: a File. POST `/api/recipes/import` using FormData with exactly the field `file` holding that file; no manually specified Content-Type or multipart boundary.
- Output extraction: only status 200 maps to `{ status: 'extracted', data }`, retaining complete/incomplete response content. Other HTTP statuses map to an error result, including unexpected 201.
- Preserve server `error` messages; missing/empty error text falls back to `Import failed (HTTP N)`. Preserve the existing invalid-error-JSON fallback `Unknown error`. Fetch rejection and invalid success JSON may reject for the page to handle; never manufacture extraction success.
- Input confirmation: existing ImportConfirmRequest. POST unchanged JSON to `/api/recipes/import/confirm` with `Content-Type: application/json`.
- Replace the broad `res.ok` acceptance with HTTP 201-only success; return parsed RecipeDetailResponse. Other statuses throw `Failed to confirm recipe import`, including otherwise successful 200/202/204. Network and success-JSON failures reject rather than return a fabricated recipe.
- File format/size enforcement remains server-owned: PNG/JPG/JPEG/WEBP, inclusive 10 MiB (`10 * 1024 * 1024` bytes, the current “10 MB” meaning). Mocks represent acceptance/rejection; introduce no conflicting browser validator.

## Files / Modules Impacted
- `frontend/src/api/client.ts`
- `frontend/src/api/client.test.ts`

## Acceptance Criteria
- Given a selected file, when extracted, then the multipart request contains the original file and no custom Content-Type.
- Given extraction HTTP 200, when parsed, then a review proposal is returned; given any tested failure, then no success result is returned.
- Given confirmation HTTP 201, when parsed, then the saved recipe is returned; given HTTP 200 or a failure, then confirmation rejects.

## Testing Requirements
Use isolated Vitest fetch spies restored per test. Exact cases: multipart key/file identity and absent headers; 200 COMPLETE and NEEDS_MORE_INFO; PNG, JPG, JPEG, WEBP forwarded unchanged; exactly 10 MiB accepted by mocked 200; unsupported GIF and 10 MiB + 1 byte rejected by mocked 400 with explanatory messages; 502 error text; missing/empty error text fallback; malformed error JSON fallback; unexpected extraction 201; rejected fetch and malformed 200 JSON; exact confirm URL/method/header/body; 201 recipe result; confirmation 200/202/204/400/500 rejection; rejected confirmation fetch and malformed 201 JSON rejection. Retain existing browsing adapter tests.

Run from `frontend`: `npm run test -- src/api/client.test.ts`.

## Dependencies / Preconditions
Existing Vitest and browser FormData support. All responses mocked; no backend prerequisite.
