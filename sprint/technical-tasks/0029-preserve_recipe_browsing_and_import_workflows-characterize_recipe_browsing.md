# Title
Characterize home browsing and category sidebar navigation

## Related User Story
- Slug: `preserve_recipe_browsing_and_import_workflows`
- Current: `sprint/user-stories/preserve_recipe_browsing_and_import_workflows.md`
- Eventual: `sprint/processed-user-stories/preserve_recipe_browsing_and_import_workflows.md`

## Objective
Strengthen the existing HomePage and CategorySidebar tests around the observable browsing contract.

## Scope
Test-only characterization of cards, counts, loading/empty states, category selection, and navigation.

## Out of Scope
Frontend sorting/limiting/filtering algorithms, production component changes, new components, styles, new locales, backend ranking tests, and architecture rewrites.

## Clean Architecture Placement
Presentation tests exercise existing React components through MemoryRouter and mocked HTTP responses from the existing adapter.

## Execution Dependencies
None.

## Implementation Details
- Inputs are URL query parameters and mocked category/summary DTO responses. Output is the existing card/sidebar DOM and router location.
- HomePage uses `/api/recipes/top` without a category and `/api/recipes?categoryId=N` with a category. It renders returned order directly; CategorySidebar similarly renders API category order directly.
- Model a collection of twelve distinct-ranked recipes in test data, with the mocked top endpoint returning its expected highest ten in descending order. Assert those ten render in that order and the two excluded recipes do not. This verifies presentation of the API contract, not server ranking. Also return fewer than ten and equal-count entries without adding a tie-break requirement.
- Supply alphabetically ordered category responses with known counts, including zero; assert DOM order and exact counts. Existing unsorted sidebar fixtures do not establish a client sorting requirement.
- Preserve card IDs, title/category values, `toLocaleString('en')` positive-count text, absence of the view span for zero, and existing active-link classes.
- Use deferred responses for `Loading...`, then resolve to content or `No recipes found.`. Rejected recipe fetch currently becomes the empty state; rejected categories leave the sidebar structure and All Recipes link.
- Click real sidebar/card links, verify category query and detail path, and verify All Recipes restores the popular request. Endpoint-specific mocks must reject unexpected requests rather than silently return an empty list.

## Files / Modules Impacted
- `frontend/src/pages/HomePage.test.tsx`
- `frontend/src/components/CategorySidebar.test.tsx`
- Reference only: `frontend/src/pages/HomePage.tsx`, `frontend/src/components/CategorySidebar.tsx`, `frontend/src/api/client.ts`.

## Acceptance Criteria
- Given a ranked top-ten response, when home loads, then exactly those cards appear in response order with correct titles and categories.
- Given alphabetical categories and counts, when displayed and selected, then their order/counts are correct and only the mocked selected-category recipes appear.
- Given loading, empty, or failed recipe responses, when rendered, then the existing corresponding loading/empty messages appear.
- Given recipe and category links, when clicked, then the existing routes and active category update correctly.

## Testing Requirements
Vitest/RTL with MemoryRouter, userEvent, endpoint-scoped fetch spies, and per-test restoration. Exact cases: ten of twelve fixture recipes; fewer than ten; equal counts preserve supplied order; exact card titles/categories; 5,432 positive views and zero-view span absent; alphabetical sidebar and counts including zero; category click changes query/request/cards; empty category; All Recipes returns popular list; card click reaches `/recipes/:id`; deferred loading then content; empty top result; rejected recipe request; rejected categories preserve All Recipes. Use existing IDs and scoped DOM assertions; settle deferred work.

Run from `frontend`: `npm run test -- src/pages/HomePage.test.tsx src/components/CategorySidebar.test.tsx`.

## Dependencies / Preconditions
Existing RTL/Vitest/router setup. No backend prerequisite; ordering and counts are supplied contract fixtures.
