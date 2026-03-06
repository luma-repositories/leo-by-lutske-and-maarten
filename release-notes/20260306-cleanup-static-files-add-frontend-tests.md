# Remove Legacy Static Files and Add Frontend Tests

## Summary
- Removed all 237 legacy static HTML/CSS/PDF files from `backend/src/main/resources/META-INF/resources/` — the recipe data is now served from the database via REST API and rendered by the React frontend.
- Updated `HomePageAvailabilityTest` to verify API endpoint health instead of the removed static HTML pages.
- Added Vitest + React Testing Library to the frontend with 43 component and unit tests covering all components, pages, and the API client.

## User Impact
- No user-facing changes — the legacy static files were already superseded by the React frontend and database-backed API.
- The original static site remains preserved in `leo-legacy-static/` for reference.

## Config/Env Changes
- Frontend `package.json`: added `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `jsdom` as dev dependencies.
- Frontend `package.json`: added `test` and `test:watch` scripts.
- Frontend `vite.config.ts`: added Vitest configuration (jsdom environment, setup file, CSS support).
- Frontend `tsconfig.app.json`: added `vitest/globals` to types.

## Test Coverage Mapping
| Component/Module | Tests | What's Covered |
|---|---|---|
| `api/client.ts` | 11 | All 5 fetch functions: success + error paths, URL construction with/without params |
| `Header.tsx` | 6 | Brand rendering, version display (success + error), flag bar, homepage link, e2e IDs |
| `Footer.tsx` | 3 | Footer text, flag bar stripes, e2e IDs |
| `CategorySidebar.tsx` | 7 | Title, "Alle recepten" link, category list rendering, counts, active highlight, error handling, e2e IDs |
| `HomePage.tsx` | 7 | Heading text (top vs category), recipe cards, loading state, empty state, view counts, e2e IDs |
| `RecipeDetailPage.tsx` | 9 | Loading, title, ingredients, preparation, category badge, section headings, 404 error, back link, e2e IDs |

## How to Verify
1. Backend: `./gradlew :backend:build` — all 14 backend tests pass.
2. Frontend tests: `cd frontend && npm test` — all 43 tests pass.
3. Frontend build: `cd frontend && npm run build` — TypeScript + Vite build succeeds.
