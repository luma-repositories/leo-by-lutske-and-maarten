# Prompt
You are a senior full-stack engineer (Quarkus + PostgreSQL + React) and DevOps-savvy with Podman. I currently have a Quarkus application that serves a website and exposes at least one REST API (e.g., `/api/version`). I want to evolve this into a local development environment using **podman compose** with a **PostgreSQL** database, and migrate the frontend from a static site + jQuery into a **React** web application. The UI theme should use the **colors of Italy** (green/white/red) in a tasteful, modern way.

High-level goals:
1) Add PostgreSQL via `podman-compose` and run Quarkus + Postgres together locally.
2) All data that used to be embedded in the static website (hardcoded JSON, JS arrays, HTML tables, etc.) must be moved into PostgreSQL.
3) The website must no longer contain hardcoded data; it should fetch all data through Quarkus REST APIs.
4) Replace the static frontend with a React app that consumes those APIs.
5) Ensure same-origin local development (or configure a clean dev proxy) so the React app can call the backend without messy CORS.

Assumptions:
- You will inspect the repository to find any data currently stored in HTML/JS (e.g., lists, cards, tables, config values) and model it in the database.
- If there is only minimal data today, create a sensible example domain model (e.g., “items” with title, description, category, createdAt) and migrate the old content into it.
- Use database migrations (Flyway or Liquibase) so the DB schema and seed data are reproducible.

Required deliverables:
A) Podman Compose environment
- Add `compose.yaml` (or `podman-compose.yml`) that starts:
    - `postgres` container (official image)
    - `quarkus` container for dev OR provide a workflow where Quarkus runs on the host and only Postgres is containerized (choose the simplest reliable approach and document it).
- Configure:
    - Persistent volume for Postgres data
    - Exposed ports (Postgres + Quarkus)
    - Healthcheck for Postgres
    - Environment variables for DB name/user/password
- Provide exact commands:
    - `podman compose up -d`
    - `podman compose down`
    - `podman volume ls` / where data lives
    - How to reset DB (drop volume) for a clean start

B) Quarkus backend changes (Postgres + API-first)
- Add Quarkus dependencies for:
    - PostgreSQL JDBC
    - Hibernate ORM with Panache (or JDBC + jOOQ; pick one and be consistent)
    - REST (RESTEasy Reactive / Quarkus REST)
    - JSON (Jackson)
    - Flyway (preferred) for schema + seed
- Update `application.properties`:
    - Datasource URL, username, password (match compose)
    - Dev services OFF (since we’re using compose)
    - Flyway migration enabled
- Create a minimal domain model based on the website’s current data:
    - Entity/table design
    - Repository/service layer
    - REST resources:
        - `GET /api/<resource>` list
        - `GET /api/<resource>/{id}` detail
        - Optional: `POST` for creating new records (only if simple)
- Replace any “hardcoded version” logic:
    - Keep `app.version` in properties if desired, but also show how the React app can display it.
- Provide OpenAPI or at least a clear API contract in the README.

C) Data migration from website to database
- Identify all data that is currently stored in the static website (HTML/JS).
- Move it into Flyway seed SQL (or migration scripts):
    - `V1__init.sql` for schema
    - `V2__seed.sql` for initial data
- Ensure running the stack reproduces the data consistently.

D) Frontend migration: static -> React
- Create a React app in the repo:
    - Prefer Vite + React + TypeScript (unless there’s a reason to stay JS)
- Implement pages/components that render the DB-backed data:
    - A list page calling `GET /api/...`
    - A detail view calling `GET /api/.../{id}`
    - A small header/footer that also displays backend version from `/api/version` (or config endpoint)
- Styling/theme:
    - Use a modern, minimal UI with Italian palette:
        - Primary accent: green
        - Secondary accent: red
        - Whites/neutral grays for background
    - Keep it tasteful (avoid looking like a flag pasted everywhere).
    - Use CSS variables or a theme file so palette is centrally managed.
- API consumption:
    - For dev: use a Vite proxy so fetches like `/api/...` go to Quarkus without CORS.
    - For prod: build React and serve it from Quarkus (recommended) OR provide a containerized Nginx option; pick one and document.

E) Integrate build + run workflow
- Dev workflow (recommended):
    1) `podman compose up -d postgres`
    2) `./mvnw quarkus:dev`
    3) `cd frontend && npm install && npm run dev` (with proxy)
- Prod-like workflow:
    - Build React (`npm run build`) and copy output into Quarkus static resources so Quarkus serves it.
    - `./mvnw package` then `java -jar ...`
- Provide a single README section “Getting started” with exact steps and URLs.

F) Output format
- Provide:
    - A step-by-step plan
    - A list of created/modified files
    - Full contents (or diffs) for:
        - compose.yaml
        - application.properties
        - Flyway migrations
        - Entity + Resource classes
        - React app key files (package.json, vite config proxy, main components, theme css)
        - README updates
- Keep the implementation minimal but complete and runnable.

Implementation choices (make them explicit and consistent):
- Backend: Quarkus REST + Panache + Flyway + Postgres
- Frontend: Vite React (TypeScript preferred)
- API base path: `/api`
- Use same-origin paths in the frontend (e.g., fetch(`/api/items`)).

Now proceed to implement this migration in the repository. Start by:
1) Inspecting the current static site to identify data that must move to DB.
2) Proposing a DB schema and API contract.
3) Implementing compose + migrations + backend.
4) Implementing React app + proxy + Italian theme.
5) Updating docs and providing run commands.