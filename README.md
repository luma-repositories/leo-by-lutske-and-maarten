# Leo Legacy Recepten

A full-stack recipe application migrating the legacy leo-legacy.be cooking website into a modern Kotlin + Quarkus backend with a React (TypeScript) frontend.

## Prerequisites

- Java 21+
- Node.js 18+ and npm
- Podman (or Docker) for local PostgreSQL
- An AI API key for recipe import (OpenAI, Anthropic, or a local vLLM endpoint)
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
│       │   │   ├── application/service/          # Extraction service interface + DTOs
│       │   │   ├── infrastructure/
│       │   │   │   ├── ai/                       # LangChain4j implementation + ChatModel producer
│       │   │   │   └── persistence/
│       │   │   │       ├── entity/               # JPA entities (Category, Recipe)
│       │   │   │       └── repository/           # Panache repositories
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── CategoryResource.kt       # GET /api/categories
│       │   │       ├── RecipeResource.kt         # GET /api/recipes, /api/recipes/top, /api/recipes/{id}
│       │   │       ├── RecipeImportResource.kt   # POST /api/recipes/import, /api/recipes/import/confirm
│       │   │       ├── VersionResource.kt        # GET /api/version
│       │   │       └── *Response.kt / *Dtos.kt   # REST DTOs
│       │   └── resources/
│       │       ├── application.properties        # Quarkus + datasource + AI config
│       │       └── db/migration/
│       │           ├── V1__init.sql              # Schema (categories + recipes)
│       │           ├── V2__seed.sql              # Seed data (14 categories, 106 recipes)
│       │           ├── V3__add_recipe_import_fields.sql
│       │           └── V4__add_import_metadata.sql
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
│       ├── i18n/useTranslation.ts                # Simple i18n hook
│       ├── locales/nl.json                       # Dutch translations
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

### 2. Configure AI Provider

Set your AI API key as an environment variable:

```bash
export AI_API_KEY=sk-your-openai-key-here
```

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

### 3. Start the Backend

```bash
./gradlew :backend:quarkusDev
```

The Quarkus backend starts on [http://localhost:8080](http://localhost:8080) with live reload. Flyway automatically runs database migrations on startup.

### 4. Start the Frontend

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

- **Backend**: Kotlin 2.0.21, Quarkus 3.17.7, LangChain4j 0.26.2, Hibernate ORM Panache, Flyway, PostgreSQL
- **Frontend**: React 19, TypeScript 5.9, Vite 7, React Router 7
- **AI**: LangChain4j with OpenAI, Anthropic Claude, and vLLM support
- **Build**: Gradle 8.12 (wrapper), npm
- **Infrastructure**: Podman Compose, PostgreSQL 17
