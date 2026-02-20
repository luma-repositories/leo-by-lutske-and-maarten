## Summary
- Added a Podman Compose PostgreSQL environment for local development.
- Migrated legacy embedded recipe data into PostgreSQL with Flyway migrations.
- Replaced the static/jQuery frontend with a React + Vite + TypeScript application.

## User impact
- Recipe content is now loaded through Quarkus APIs instead of hardcoded HTML data.
- The new frontend uses a modern Italian-inspired visual theme (green/white/red accents).

## Config/env changes
- Added Postgres datasource configuration in `application.properties`.
- Added compose service credentials for local DB:
  - DB: `leolegacy`
  - User: `leolegacy`
  - Password: `leolegacy`

## Platform dependency notes
- Added backend dependencies:
  - `quarkus-hibernate-orm-panache`
  - `quarkus-jdbc-postgresql`
  - `quarkus-flyway`
  - `quarkus-jdbc-h2` (test profile)

## Migration notes
- Legacy website files were preserved under `src/main/resources/legacy-site/`.
- Quarkus served static root now targets built React assets in `META-INF/resources`.

## How to verify
1. `podman compose up -d postgres`
2. `./mvnw test`
3. `./mvnw quarkus:dev`
4. `cd frontend && npm install && npm run dev`
5. Open `http://localhost:5173/` and confirm data loads from `/api`.
6. Build prod-like: `npm run build:quarkus && cd .. && ./mvnw package`.
