# Leo Legacy Recepten

A full-stack recipe application migrating the legacy leo-legacy.be cooking website into a modern Kotlin + Quarkus backend with a React (TypeScript) frontend.

## Prerequisites

- Java 21+
- Node.js 18+ and npm
- Podman (or Docker) for local PostgreSQL
- Tesseract OCR (for recipe import from image feature)
  - macOS: `brew install tesseract`
  - Ubuntu/Debian: `sudo apt install tesseract-ocr`
  - The `TESSDATA_PREFIX` environment variable can be set if tessdata is not in the default location
- No global Gradle installation needed (uses Gradle wrapper)

## Project Structure

```
├── build.gradle.kts                              # Root build file (thin)
├── settings.gradle.kts                           # Gradle settings (includes backend)
├── gradle.properties                             # Gradle/Quarkus properties
├── platform/
│   └── quarkus-platform.gradle                   # Centralized dependency versions
├── compose.yaml                                  # PostgreSQL 17 (Podman/Docker Compose)
├── backend/
│   ├── build.gradle.kts                          # Backend build file (Quarkus + JPA)
│   └── src/
│       ├── main/
│       │   ├── kotlin/be/lutske/leolegacy/
│       │   │   ├── infrastructure/persistence/
│       │   │   │   ├── entity/                   # JPA entities (Category, Recipe)
│       │   │   │   └── repository/               # Panache repositories
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── CategoryResource.kt       # GET /api/categories
│       │   │       ├── RecipeResource.kt         # GET /api/recipes, /api/recipes/top, /api/recipes/{id}
│       │   │       ├── RecipeImportResource.kt  # POST /api/recipes/import, /api/recipes/import/confirm
│       │   │       ├── VersionResource.kt        # GET /api/version
│       │   │       └── *Response.kt / *Dtos.kt  # REST DTOs
│       │   └── resources/
│       │       ├── application.properties        # Quarkus + datasource config
│       │       └── db/migration/
│       │           ├── V1__init.sql              # Schema (categories + recipes)
│       │           └── V2__seed.sql              # Seed data (14 categories, 106 recipes)
│       └── test/
│           ├── kotlin/be/lutske/leolegacy/       # Backend tests (H2 in-memory)
│           └── resources/application.properties  # H2 test config
├── frontend/
│   ├── package.json                              # Vite + React + TypeScript
│   ├── vite.config.ts                            # Dev proxy /api -> localhost:8080
│   └── src/
│       ├── main.tsx                              # App entry point
│       ├── App.tsx                               # Layout (Header + Routes + Footer)
│       ├── theme.css                             # Italian color palette (green/white/red)
│       ├── api/client.ts                         # API types + fetch helpers
│       ├── components/                           # Header, Footer, CategorySidebar
│       └── pages/                                # HomePage, RecipeDetailPage, ImportRecipePage
├── release-notes/                                # Release notes per change
└── leo-legacy-static/                            # Original static site (reference)
```

## How to Run (Development)

### 1. Start PostgreSQL

```bash
podman compose up -d
```

This starts a PostgreSQL 17 container on port 5432 with persistent volume.

### 2. Start the Backend

```bash
./gradlew :backend:quarkusDev
```

The Quarkus backend starts on [http://localhost:8080](http://localhost:8080) with live reload. Flyway automatically runs database migrations on startup.

### 3. Start the Frontend

```bash
cd frontend
npm install    # first time only
npm run dev
```

The Vite dev server starts on [http://localhost:3000](http://localhost:3000) and proxies `/api` requests to the backend.

## Build & Test

### Backend

```bash
./gradlew :backend:build    # compile + test + package
./gradlew :backend:test     # tests only (uses H2 in-memory, no Docker needed)
```

### Frontend

```bash
cd frontend
npm run build    # TypeScript check + Vite production build
npm run lint     # ESLint
```

## API Endpoints

| Method | Path                          | Description                                                  |
|--------|-------------------------------|--------------------------------------------------------------|
| GET    | `/api/version`                | Application version as JSON                                  |
| GET    | `/api/categories`             | All categories with recipe counts                            |
| GET    | `/api/recipes`                | All recipes (optional `?categoryId=N` filter)                |
| GET    | `/api/recipes/top`            | Top 10 most-viewed recipes                                   |
| GET    | `/api/recipes/{id}`           | Full recipe detail (ingredients + preparation)               |
| POST   | `/api/recipes/import`         | Upload image for OCR recipe import (multipart/form-data)     |
| POST   | `/api/recipes/import/confirm` | Confirm and save an imported recipe with user corrections     |

### Example: GET /api/categories

```json
[
  { "id": 1, "name": "Aperitief hapjes", "recipeCount": 12 },
  { "id": 2, "name": "Soepen", "recipeCount": 8 }
]
```

### Example: GET /api/recipes/1

```json
{
  "id": 1,
  "title": "Gevulde champignons",
  "ingredients": ["250 g champignons", "100 g roomkaas", "..."],
  "preparation": "Verwarm de oven op 200 graden...",
  "categoryId": 1,
  "categoryName": "Aperitief hapjes",
  "viewCount": 1234
}
```

### Recipe Import from Image

Upload a photo of a recipe and the backend uses Tesseract OCR to extract text, then parses it into a structured recipe. The flow:

1. **Upload**: `POST /api/recipes/import` with a multipart image file (PNG, JPG, WEBP, max 10 MB)
2. **Full parse → 201**: If title, ingredients, and preparation are all detected, the recipe is saved and returned
3. **Needs more info → 422**: If fields are missing, returns the raw OCR text, a proposed recipe, missing fields list, and parse warnings
4. **Confirm**: `POST /api/recipes/import/confirm` with the proposed recipe + user overrides → saves and returns the final recipe

Imported recipes are assigned to the "Geimporteerd" category. The parser supports both English and Dutch section headings (Ingredients/Benodigdheden, Instructions/Bereiding, etc.).

## Database

- **PostgreSQL 17** in development (via Podman Compose)
- **H2 in-memory** (PostgreSQL compatibility mode) for tests
- **Flyway** manages schema migrations in `backend/src/main/resources/db/migration/`
- 15 categories (14 original + "Geimporteerd") and 106 recipes seeded from the original static site

## Configuration

Key application properties (`backend/src/main/resources/application.properties`):

| Property                              | Default                                          | Description                |
|---------------------------------------|--------------------------------------------------|----------------------------|
| `app.version`                         | `1.2.3`                                          | Application version        |
| `quarkus.datasource.jdbc.url`         | `jdbc:postgresql://localhost:5432/leo_legacy`     | Database connection URL    |
| `quarkus.datasource.username`         | `leo`                                            | Database username          |
| `quarkus.datasource.password`         | `leo`                                            | Database password          |
| `quarkus.flyway.migrate-at-start`     | `true`                                           | Auto-run migrations        |
| `ocr.tessdata-path`                   | `/opt/homebrew/share/tessdata`                   | Path to Tesseract tessdata |
| `ocr.language`                        | `nld`                                            | OCR language (nld=Dutch)   |
| `quarkus.http.limits.max-body-size`   | `10M`                                            | Max upload size            |

## Tech Stack

- **Backend**: Kotlin 2.0.21, Quarkus 3.17.7, Hibernate ORM Panache, Flyway, PostgreSQL, Tess4J (OCR)
- **Frontend**: React 19, TypeScript 5.9, Vite 7, React Router 7
- **Build**: Gradle 8.12 (wrapper), npm
- **Infrastructure**: Podman Compose, PostgreSQL 17
