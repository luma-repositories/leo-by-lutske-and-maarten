# leo-by-lutske-and-maarten

Quarkus + PostgreSQL + React migration of the legacy recipe website.

## Stack

- Backend: Quarkus REST + Panache ORM + Flyway
- Database: PostgreSQL (Podman Compose)
- Frontend: React + Vite + TypeScript
- API base path: `/api`

## Data model

- `categories`
  - `id`, `slug`, `name`, `display_order`
- `recipes`
  - `id`, `legacy_id`, `title`, `category_id`, `ingredients`, `preparation`, `pdf_slug`, `created_at`

Legacy HTML recipe data is migrated to SQL seeds using Flyway migrations.

## API contract

- `GET /api/version`
  - Response: `{ "version": "1.2.3" }`
- `GET /api/categories`
  - Response: `[{ id, slug, name, recipeCount }]`
- `GET /api/recipes`
  - Response: `[{ id, legacyId, title, category, excerpt }]`
  - Optional query: `?category=<slug>`
- `GET /api/recipes/{id}`
  - Response: `{ id, legacyId, title, category, ingredients, preparation, pdfSlug }`

## Getting started

### 1) Start PostgreSQL (Podman)

```bash
podman compose up -d postgres
```

Useful commands:

```bash
podman compose down
podman volume ls
podman compose down -v   # reset DB (drop volume/data)
```

### 2) Run Quarkus backend

```bash
./mvnw quarkus:dev
```

Backend URLs:

- `http://localhost:8080/api/version`
- `http://localhost:8080/api/categories`
- `http://localhost:8080/api/recipes`

### 3) Run React frontend (dev)

```bash
cd frontend
npm install
npm run dev
```

Frontend tests:

```bash
npm test
npm run test:watch
```

Frontend URL:

- `http://localhost:5173/`

Vite proxies `/api/*` to Quarkus at `http://localhost:8080`, so no CORS setup is needed.

## Prod-like workflow

1. Build frontend and copy into Quarkus static resources:

```bash
cd frontend
npm run build:quarkus
```

2. Build backend and run packaged app:

```bash
cd ..
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/api/version`

## Migrations

- `src/main/resources/db/migration/V1__init.sql`
- `src/main/resources/db/migration/V2__seed.sql`

Flyway runs automatically at startup.

## Troubleshooting

- If Quarkus fails with `configured datasource <default> not found`, verify you are using the repository `src/main/resources/application.properties` and start from a clean run:

```bash
podman compose up -d postgres
./mvnw clean quarkus:dev
```

- Also verify no conflicting environment overrides are active (especially empty values):

```bash
printenv QUARKUS_PROFILE QUARKUS_DATASOURCE_DB_KIND QUARKUS_DATASOURCE_JDBC_URL APP_DATASOURCE_JDBC_URL
```

If needed, clear overrides and retry:

```bash
unset QUARKUS_PROFILE QUARKUS_DATASOURCE_DB_KIND QUARKUS_DATASOURCE_JDBC_URL QUARKUS_DATASOURCE_USERNAME QUARKUS_DATASOURCE_PASSWORD
```

- If the UI shows a recipes load error, check backend availability at `http://localhost:8080/api/recipes` and database container status (`podman compose ps`).
