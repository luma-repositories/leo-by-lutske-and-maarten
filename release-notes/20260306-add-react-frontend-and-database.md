# Add React Frontend and Database Backend

## Summary
- Added a PostgreSQL database with Flyway migrations containing all 106 recipes across 14 categories extracted from the legacy static site.
- Built a Kotlin/Quarkus REST API serving categories, recipes, top recipes, and recipe detail endpoints.
- Created a React (TypeScript + Vite) frontend with an Italian-themed UI (green/white/red color palette) that fetches all data from the backend API.
- Added Podman Compose configuration for local PostgreSQL development.

## User Impact
- The recipe website is now a full-stack application: React frontend at `http://localhost:3000` (dev) proxying API calls to the Quarkus backend at `http://localhost:8080`.
- Users can browse all 106 recipes by category, view the top 10 most popular recipes, and see full recipe details (ingredients + preparation).
- The UI features a modern Italian-inspired design with responsive layout.

## Config/Env Changes
- New `compose.yaml` for PostgreSQL 17 (port 5432, user: `leo`, password: `leo`, database: `leo_legacy`).
- Backend `application.properties` updated with datasource, Flyway, and Hibernate ORM configuration.
- Test `application.properties` uses H2 in-memory database (PostgreSQL mode) so tests run without Docker.
- Frontend Vite dev server on port 3000 with `/api` proxy to `localhost:8080`.

## Platform Dependency Notes
- `platform/quarkus-platform.gradle` extended with:
  - `quarkus-hibernate-orm-panache-kotlin` (JPA + Panache)
  - `quarkus-jdbc-postgresql` (PostgreSQL JDBC driver)
  - `quarkus-flyway` (database migrations)
  - `com.h2database:h2` (test-only, in-memory database)
- `backend/build.gradle.kts` updated with `plugin.jpa` and new dependencies referencing platform versions.

## Migration Notes
- Recipe data was extracted from the static HTML files in `leo-legacy-static/` using regex parsing.
- Ingredients are stored pipe-separated (`|`) in the database and split into arrays by the REST API.
- Flyway migrations are H2-compatible (no `BIGSERIAL`, no `E''` escape syntax) so tests work without PostgreSQL.
- The original static site files in `backend/src/main/resources/META-INF/resources/` are still present and can be removed in a future cleanup.

## Frontend Components
- **Header**: Sticky header with Italian flag bar, brand link, and version display.
- **Footer**: Dark footer with Italian flag accent bar.
- **CategorySidebar**: Navigation sidebar listing all 14 categories with recipe counts.
- **HomePage**: Grid of recipe cards, showing top recipes by default or filtered by category.
- **RecipeDetailPage**: Full recipe view with ingredients list and preparation text.

## How to Verify
1. Start PostgreSQL: `podman compose up -d`
2. Start backend: `./gradlew :backend:quarkusDev`
3. Start frontend: `cd frontend && npm run dev`
4. Open `http://localhost:3000` — homepage should show top recipes with category sidebar.
5. Click a category — recipes filter by category.
6. Click a recipe — detail page shows ingredients and preparation.
7. Run backend tests: `./gradlew :backend:test` — all 14 tests pass (uses H2, no Docker needed).
8. Build frontend: `cd frontend && npm run build` — TypeScript + Vite build succeeds.
