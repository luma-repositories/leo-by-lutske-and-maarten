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
- `POST /api/recipes/import`
  - Content-Type: `multipart/form-data`
  - Form field: `file`
  - `201 Created`: full recipe DTO
  - `422 Unprocessable Entity`: `{ status: "NEEDS_MORE_INFO", rawText, proposedRecipe, missingFields, parseWarnings }`
- `POST /api/recipes/import/confirm`
  - Content-Type: `application/json`
  - Body: `{ rawText, proposedRecipe, userOverrides }`
  - `201 Created`: full recipe DTO
  - `422 Unprocessable Entity`: same needs-more-info structure

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

OCR prerequisite (Tess4J runtime):

```bash
brew install tesseract
export DYLD_LIBRARY_PATH="$(brew --prefix)/lib:$DYLD_LIBRARY_PATH"
export TESSDATA_PREFIX="$(brew --prefix tesseract)/share/tessdata"
```

Optional OCR environment settings:

```bash
export APP_IMPORT_OCR_LANGUAGE=eng
export APP_IMPORT_MAX_FILE_SIZE_BYTES=8388608
export APP_IMPORT_OCR_STUB_TEXT="" # optional: test OCR flow without native tesseract
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
- `src/main/resources/db/migration/V3__add_recipe_import_support.sql`

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
- If OCR import fails, verify Tesseract is installed and `TESSDATA_PREFIX` points to a directory containing `eng.traineddata`.
- On macOS, if upload returns `OCR runtime unavailable` or logs `Unable to load library 'tesseract'`, export `DYLD_LIBRARY_PATH="$(brew --prefix)/lib:$DYLD_LIBRARY_PATH"` in the same shell before starting Quarkus.
- For frontend-only flow testing without native OCR, set `APP_IMPORT_OCR_STUB_TEXT="Scanned recipe"` before starting Quarkus.
- Missing OCR environment variables no longer block Quarkus startup; OCR properties now use safe runtime fallbacks.

## Manual import verification

1. `podman compose up -d`
2. `./mvnw quarkus:dev`
3. `cd frontend && npm run dev`
4. Open `http://localhost:5173/recipes/import`
5. Upload a clear recipe image and verify you are redirected to recipe detail.
6. Upload a messy image and verify the follow-up form appears with OCR raw text and warnings.
7. Complete missing fields, click confirm, and verify the recipe is created.
8. Restart services and verify imported recipes remain available.
