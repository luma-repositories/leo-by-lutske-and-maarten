# Title
Characterize exact recipe details and return navigation

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Prove existing saved content, category return, and not-found behavior are preserved without adding a view increment request.

## Scope
Extend RecipeDetailPage tests only, using the real existing HomePage for category-return coverage where useful.

## Out of Scope
Production changes, recipe editing, metric conversion, view tracking, new components, and backend persistence tests.

## Clean Architecture Placement
Presentation characterization through existing routes and HTTP adapter; no new application layer for read-only detail rendering.

## Execution Dependencies
None.

## Implementation Details
- Input is `/recipes/:id` and a mocked RecipeDetailResponse; output is unchanged title, category, ordered ingredient list, and preparation text.
- Assert exact list item text/order and preparation `textContent`, including line breaks and a blank line before `Notes: Serve chilled.`; avoid matchers that normalize away meaningful whitespace.
- Include saved metric, already-converted, imperial, and vague ingredient strings to prove detail rendering does not transform saved content.
- The return link uses `/?categoryId=${recipe.categoryId}` and `Back to ${recipe.categoryName}`. Click it and assert HomePage requests and displays that category's fixture recipes.
- A deferred read shows `Loading...`. A 404, server failure, or rejected fetch shows `Recipe not found.` and the `/` back link; click through to home.
- Inspect all fetch calls before return navigation: only `/api/recipes/:id` with default GET is expected. No POST, PATCH, PUT, or extra view-increment request is allowed. Do not claim this proves backend counts are immutable.

## Files / Modules Impacted
- `frontend/src/pages/RecipeDetailPage.test.tsx`
- Reference only: `frontend/src/pages/RecipeDetailPage.tsx`, `frontend/src/pages/HomePage.tsx`, `frontend/src/components/CategorySidebar.tsx`, `frontend/src/api/client.ts`.

## Acceptance Criteria
- Given saved multiline content, when detail opens, then all fields and ingredient order match the response exactly.
- Given a displayed recipe, when the category-return link is clicked, then the correct category list appears.
- Given a failed detail read, when settled, then the not-found message and working home link appear.
- Given merely opening detail, when requests are inspected, then no increment or mutation request occurred.

## Testing Requirements
Isolated Vitest/RTL tests with MemoryRouter Routes, userEvent, restored fetch spies, and explicit endpoint mocks. Exact cases: deferred loading resolves; exact title/category; ingredient array order and unmodified quantities; exact multiline preparation with notes; category link label/href and click into category contents; 404/500/network rejection not-found states; not-found home click; initial request log contains only the detail GET. Settle all deferred promises and preserve current IDs/classes/text.

Run from `frontend`: `npm run test -- src/pages/RecipeDetailPage.test.tsx`.

## Dependencies / Preconditions
Existing pages and test libraries; no dependency on task 0029 fixtures and no backend prerequisite.
