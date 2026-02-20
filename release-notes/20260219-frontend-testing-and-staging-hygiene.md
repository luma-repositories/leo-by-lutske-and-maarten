## Summary
- Added a staging hygiene pass to reduce accidental commit noise.
- Added frontend quality gates with Vitest and React Testing Library.
- Added sample smoke and component tests for the React app.

## User impact
- No API or UI behavior change for end users.
- Team now gets automated frontend confidence checks before commit/release.

## Config/env changes
- Updated ignore rules for local/editor/build artifacts.
- Added frontend test scripts: `npm test` and `npm run test:watch`.

## Platform dependency notes
- Added frontend dev dependencies:
  - `vitest`
  - `jsdom`
  - `@testing-library/react`
  - `@testing-library/user-event`

## Migration notes
- No data or API contract migration required.
- No backend runtime dependency changes.

## How to verify
1. `cd frontend && npm install`
2. `npm test`
3. `npm run build`
4. `cd .. && ./mvnw test`
5. `git status --short` and confirm build artifacts/noise files are excluded.
