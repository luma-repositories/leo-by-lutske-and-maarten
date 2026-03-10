# Leo Legacy Recepten

A full-stack recipe application migrating the legacy leo-legacy.be cooking website into a modern Java 25 + Quarkus backend with a React (TypeScript) frontend.

## Prerequisites

- **Java 25** (OpenJDK 25) — see [Installing Java 25 with SDKMAN](#installing-java-25-with-sdkman) below
- **Node.js 18+** and **npm** (for the frontend build)
- **Podman** (or Docker) for local PostgreSQL
- An AI API key for recipe import (OpenAI, Anthropic, or a local vLLM endpoint)
- No global Gradle installation needed (uses Gradle wrapper)

## Installing Java 25 with SDKMAN

[SDKMAN](https://sdkman.io/) is the easiest way to install and manage multiple JDK versions side by side.

### 1. Install SDKMAN

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Verify the installation:

```bash
sdk version
```

### 2. Install OpenJDK 25

List available Java 25 builds:

```bash
sdk list java | grep '25'
```

Install the latest OpenJDK 25 build:

```bash
sdk install java 25.0.2-open
```

### 3. Enable OpenJDK 25

To use Java 25 **in the current terminal session**:

```bash
sdk use java 25.0.2-open
```

To set Java 25 as **your default** (all new terminals):

```bash
sdk default java 25.0.2-open
```

### 4. Verify

```bash
java -version
```

You should see output like:

```
openjdk version "25.0.2" 2026-01-20
OpenJDK Runtime Environment (build 25.0.2+10-69)
OpenJDK 64-Bit Server VM (build 25.0.2+10-69, mixed mode, sharing)
```

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
│       │   ├── java/be/lutske/leolegacy/
│       │   │   ├── application/service/
│       │   │   │   ├── RecipeExtractionService.java    # AI extraction interface
│       │   │   │   ├── ExtractionResult.java           # AI extraction result record
│       │   │   │   └── RecipeExtractionException.java  # AI extraction exception
│       │   │   ├── infrastructure/
│       │   │   │   ├── ai/
│       │   │   │   │   ├── LangChain4jRecipeExtractionService.java  # LangChain4j implementation
│       │   │   │   │   └── ChatModelProducer.java                   # CDI ChatModel producer
│       │   │   │   └── persistence/
│       │   │   │       ├── entity/
│       │   │   │       │   ├── RecipeEntity.java       # JPA recipe entity
│       │   │   │       │   └── CategoryEntity.java     # JPA category entity
│       │   │   │       └── repository/
│       │   │   │           ├── RecipeRepository.java   # Panache recipe repository
│       │   │   │           └── CategoryRepository.java # Panache category repository
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── CategoryResource.java           # GET /api/categories
│       │   │       ├── RecipeResource.java             # GET /api/recipes, /api/recipes/top, /api/recipes/{id}
│       │   │       ├── RecipeImportResource.java       # POST /api/recipes/import, /api/recipes/import/confirm
│       │   │       ├── RecipeImportDtos.java           # Import flow DTOs (nested records)
│       │   │       ├── VersionResource.java            # GET /api/version
│       │   │       ├── RecipeDetailResponse.java       # Recipe detail DTO
│       │   │       ├── RecipeSummaryResponse.java      # Recipe list DTO
│       │   │       ├── CategoryResponse.java           # Category DTO
│       │   │       ├── VersionResponse.java            # Version DTO
│       │   │       └── SpaRoutingFilter.java           # SPA routing filter
│       │   └── resources/
│       │       ├── application.properties              # Quarkus + datasource + AI config
│       │       └── db/migration/
│       │           ├── V1__init.sql                    # Schema (categories + recipes)
│       │           ├── V2__seed.sql                    # Seed data (14 categories, 106 recipes)
│       │           ├── V3__add_recipe_import_fields.sql# Import fields (source, created_at)
│       │           └── V4__add_import_metadata.sql     # AI import metadata column
│       └── test/
│           ├── java/be/lutske/leolegacy/
│           │   ├── application/service/
│           │   │   └── ExtractionResultValidationTest.java    # ExtractionResult unit tests
│           │   └── interfaceadapter/rest/
│           │       ├── VersionResourceTest.java
│           │       ├── CategoryResourceTest.java
│           │       ├── RecipeResourceTest.java
│           │       ├── RecipeImportResourceTest.java       # Import tests (mocked AI extraction)
│           │       ├── RecipeImportIntegrationTest.java    # E2E import (real Postgres + AI, @Tag("integration"))
│           │       └── PostgresIntegrationTestProfile.java # Test profile for real DB
│           └── resources/
│               ├── application.properties              # H2 test config
│               └── testdata/test-recipe.jpg            # Handwritten recipe image for E2E
├── frontend/
│   ├── package.json                                    # Vite + React + TypeScript
│   ├── vite.config.ts                                  # Dev proxy /api -> localhost:8080
│   └── src/
│       ├── main.tsx                                    # App entry point
│       ├── App.tsx                                     # Layout (Header + Routes + Footer)
│       ├── theme.css                                   # Italian color palette (green/white/red)
│       ├── api/client.ts                               # API types + fetch helpers
│       ├── i18n/useTranslation.ts                      # Simple i18n hook
│       ├── locales/nl.json                             # Dutch translations
│       ├── components/                                 # Header, Footer, CategorySidebar
│       └── pages/                                      # HomePage, RecipeDetailPage, ImportRecipePage
└── release-notes/                                      # Release notes per change
```

## How to Run

### 1. Start PostgreSQL

```bash
podman compose up -d
# or: docker compose up -d
```

This starts a PostgreSQL 17 container on port 5432 with persistent volume.

### 2. Configure AI Provider

Copy the environment template and fill in your API key:

```bash
cp backend/.env.example backend/.env
# Edit backend/.env and set AI_API_KEY to your OpenAI or Anthropic key
```

Quarkus reads the `backend/.env` file automatically at startup (via SmallRye Config). The `.env` file is gitignored so secrets are never committed.

The default provider is OpenAI with `gpt-4o`. To use a different provider, edit `backend/src/main/resources/application.properties` or use Quarkus profiles:

```properties
# OpenAI (default)
app.ai.provider=openai
app.ai.model=gpt-4o

# Anthropic Claude
app.ai.provider=claude
app.ai.model=claude-sonnet-4-20250514

# Local vLLM (OpenAI-compatible API)
app.ai.provider=vllm
app.ai.model=llava-v1.6-mistral-7b
app.ai.base-url=http://localhost:8000/v1
app.ai.api-key=not-needed
```

**Note for vLLM**: The served model must be vision-capable (e.g. LLaVA, Qwen-VL). If the model does not support image input, the extraction will fail with a clear error.

### 3. Start the Application

```bash
./gradlew :backend:quarkusDev
```

The Gradle build automatically builds the React frontend and bundles it into the Quarkus application. The full site (frontend + API) is served on [http://localhost:8080](http://localhost:8080). Flyway automatically runs database migrations on startup.

### 4. Frontend Development (optional — for hot reload)

For frontend-only development with hot module replacement:

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

Integration tests (tagged `@Tag("integration")`) are excluded from the default test run. To run them, start PostgreSQL first and then:

```bash
podman compose up -d
./gradlew :backend:test -Dtest.includeTags=integration
```

### Frontend

```bash
cd frontend
npm run build    # TypeScript check + Vite production build
npm run test     # Vitest unit tests
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
| POST   | `/api/recipes/import`         | Upload image for AI recipe extraction (multipart/form-data)  |
| POST   | `/api/recipes/import/confirm` | Confirm and save an imported recipe with user corrections     |

### Recipe Import from Image (AI-powered)

Upload a photo of a recipe and the backend uses a multimodal LLM (via LangChain4j) to extract structured recipe data. The flow:

1. **Upload**: `POST /api/recipes/import` with a multipart image file (PNG, JPG, WEBP, max 10 MB)
2. **Full extraction → 201**: If title, ingredients, and preparation are all detected, the recipe is saved and returned
3. **Needs more info → 422**: If fields are missing, returns the raw model response, a proposed recipe, missing fields list, and warnings
4. **Confirm**: `POST /api/recipes/import/confirm` with the proposed recipe + user overrides → saves and returns the final recipe

Imported recipes are assigned to the "Geimporteerd" category. The LLM preserves the original language of the recipe.

#### Supported AI Providers

| Provider | Config value | Notes |
|----------|-------------|-------|
| OpenAI | `openai` | GPT-4o, GPT-4-turbo, etc. Requires API key. |
| Anthropic Claude | `claude` | Claude Sonnet, Opus, etc. Requires API key. |
| vLLM (local) | `vllm` | Any OpenAI-compatible endpoint. Requires `app.ai.base-url`. Model must be vision-capable. |

## Database

- **PostgreSQL 17** in development (via Podman Compose)
- **H2 in-memory** (PostgreSQL compatibility mode) for tests
- **Flyway** manages schema migrations in `backend/src/main/resources/db/migration/`
- 15 categories (14 original + "Geimporteerd") and 106 recipes seeded from the original static site

## Configuration

Key application properties (`backend/src/main/resources/application.properties`):

| Property                              | Default                                          | Description                     |
|---------------------------------------|--------------------------------------------------|---------------------------------|
| `app.version`                         | `1.2.3`                                          | Application version             |
| `quarkus.datasource.jdbc.url`         | `jdbc:postgresql://localhost:5432/leo_legacy`     | Database connection URL         |
| `quarkus.datasource.username`         | `leo`                                            | Database username               |
| `quarkus.datasource.password`         | `leo_secret`                                     | Database password               |
| `quarkus.flyway.migrate-at-start`     | `true`                                           | Auto-run migrations             |
| `app.ai.provider`                     | `openai`                                         | AI provider (openai/claude/vllm)|
| `app.ai.model`                        | `gpt-4o`                                         | Model name                      |
| `app.ai.api-key`                      | `${AI_API_KEY}`                                  | API key (env-backed)            |
| `app.ai.base-url`                     | —                                                | Base URL (required for vllm)    |
| `app.ai.timeout-seconds`             | `120`                                            | Request timeout                 |
| `app.ai.temperature`                  | `0.1`                                            | LLM temperature                 |
| `app.ai.max-tokens`                   | `4096`                                           | Max response tokens             |
| `quarkus.http.limits.max-body-size`   | `10M`                                            | Max upload size                 |

## Tech Stack

- **Backend**: Java 25 (OpenJDK), Quarkus 3.32.2, LangChain4j 1.0.0-beta2, Hibernate ORM Panache, Flyway, PostgreSQL
- **Frontend**: React 19, TypeScript 5.9, Vite 7, React Router 7
- **AI**: LangChain4j with OpenAI, Anthropic Claude, and vLLM support
- **Build**: Gradle 9.3.1 (wrapper), npm
- **Infrastructure**: Podman Compose, PostgreSQL 17
