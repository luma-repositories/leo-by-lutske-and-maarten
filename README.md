# Leo Legacy Recepten

A full-stack recipe application migrating the legacy leo-legacy.be cooking website into a modern Java 25 + Quarkus backend with a React (TypeScript) frontend. The built frontend is bundled into the Quarkus application and served on a single port.

## Prerequisites

- **Java 25** (OpenJDK 25) — see [Installing Java 25 with SDKMAN](#installing-java-25-with-sdkman) below
- **Node.js 18+** and **npm** (for the frontend build)
- **Podman** (or Docker) for local PostgreSQL
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
│   ├── build.gradle.kts                          # Backend build (Quarkus + frontend bundling)
│   └── src/
│       ├── main/
│       │   ├── java/be/lutske/leolegacy/
│       │   │   ├── infrastructure/persistence/
│       │   │   │   ├── entity/                   # JPA entities (CategoryEntity, RecipeEntity)
│       │   │   │   └── repository/               # Panache repositories
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── CategoryResource.java     # GET /api/categories
│       │   │       ├── CategoryResponse.java     # Category DTO (record)
│       │   │       ├── RecipeResource.java       # GET /api/recipes, /top, /{id}
│       │   │       ├── RecipeDetailResponse.java # Recipe detail DTO (record)
│       │   │       ├── RecipeSummaryResponse.java# Recipe summary DTO (record)
│       │   │       ├── VersionResource.java      # GET /api/version
│       │   │       ├── VersionResponse.java      # Version DTO (record)
│       │   │       └── SpaRoutingFilter.java     # SPA routing (index.html fallback)
│       │   └── resources/
│       │       ├── application.properties        # Quarkus + datasource config
│       │       └── db/migration/
│       │           ├── V1__init.sql              # Schema (categories + recipes)
│       │           └── V2__seed.sql              # Seed data (14 categories, 106 recipes)
│       └── test/
│           ├── java/be/lutske/leolegacy/         # Backend tests (H2 in-memory)
│           │   └── interfaceadapter/rest/
│           │       ├── VersionResourceTest.java
│           │       ├── CategoryResourceTest.java
│           │       └── RecipeResourceTest.java
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
│       └── pages/                                # HomePage, RecipeDetailPage
└── release-notes/                                # Release notes per change
```

## How to Run

### 1. Start PostgreSQL

```bash
podman compose up -d
# or: docker compose up -d
```

This starts a PostgreSQL 17 container on port 5432 with persistent volume.

### 2. Start the application

```bash
./gradlew :backend:quarkusDev
```

The Gradle build automatically builds the React frontend and bundles it into the Quarkus application. The full site (frontend + API) is served on [http://localhost:8080](http://localhost:8080). Flyway automatically runs database migrations on startup.

### 3. Frontend development (optional — for hot reload)

For frontend-only development with hot module replacement:

```bash
cd frontend
npm install    # first time only
npm run dev
```

This starts a Vite dev server on [http://localhost:3000](http://localhost:3000) with API proxy to the backend on 8080.

## Build & Test

### Backend (includes frontend bundling)

```bash
./gradlew :backend:build    # compile + frontend build + test + package
./gradlew :backend:test     # tests only (uses H2 in-memory, no Docker needed)
```

### Frontend only

```bash
cd frontend
npm run build    # TypeScript check + Vite production build
npm run test     # Vitest unit/component tests
npm run lint     # ESLint
```

## API Endpoints

| Method | Path                | Description                                    |
|--------|---------------------|------------------------------------------------|
| GET    | `/api/version`      | Application version as JSON                    |
| GET    | `/api/categories`   | All categories with recipe counts              |
| GET    | `/api/recipes`      | All recipes (optional `?categoryId=N` filter)  |
| GET    | `/api/recipes/top`  | Top 10 most-viewed recipes                     |
| GET    | `/api/recipes/{id}` | Full recipe detail (ingredients + preparation) |

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

## Database

- **PostgreSQL 17** in development (via Podman Compose)
- **H2 in-memory** (PostgreSQL compatibility mode) for tests
- **Flyway** manages schema migrations in `backend/src/main/resources/db/migration/`
- 14 categories and 106 recipes seeded from the original static site

## Configuration

Key application properties (`backend/src/main/resources/application.properties`):

| Property                              | Default                                          | Description                |
|---------------------------------------|--------------------------------------------------|----------------------------|
| `app.version`                         | `1.2.3`                                          | Application version        |
| `quarkus.datasource.jdbc.url`         | `jdbc:postgresql://localhost:5432/leo_legacy`     | Database connection URL    |
| `quarkus.datasource.username`         | `leo`                                            | Database username          |
| `quarkus.datasource.password`         | `leo_secret`                                     | Database password          |
| `quarkus.flyway.migrate-at-start`     | `true`                                           | Auto-run migrations        |

## Tech Stack

- **Backend**: Java 25, Quarkus 3.32.2, Hibernate ORM Panache, Flyway, PostgreSQL
- **Frontend**: React 19, TypeScript 5.9, Vite 7, React Router 7
- **Build**: Gradle 9.3.1 (wrapper), npm
- **Infrastructure**: Podman Compose, PostgreSQL 17
