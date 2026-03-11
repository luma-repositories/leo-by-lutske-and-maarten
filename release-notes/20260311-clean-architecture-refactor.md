# Clean Architecture Refactor

**Date:** 2026-03-11

## Summary

Refactored the entire application (backend + frontend) toward Clean Architecture principles
with clear separation between domain, use cases, infrastructure, and entrypoints/presentation.

## Backend Changes

### New Domain Layer (`domain/`)
- **`domain.model`**: Framework-agnostic `Recipe`, `Category`, `RecipeExtraction` models
- **`domain.port`**: Interfaces for `RecipeRepository`, `CategoryRepository`, `RecipeExtractionService`
- **`domain.exception`**: `RecipeExtractionException`

### New Use Case Layer (`usecase/`)
- `ListRecipes` — list/filter recipes by category or top-viewed
- `GetRecipeDetail` — fetch single recipe by ID
- `ListCategories` — list categories with recipe counts
- `ExtractRecipeFromImage` — orchestrate AI extraction
- `ConfirmRecipeImport` — validate and persist imported recipe

### Infrastructure Adapters
- `PanacheRecipeRepository` — implements `RecipeRepository` port
- `PanacheCategoryRepository` — implements `CategoryRepository` port
- `LangChain4jRecipeExtractionAdapter` — implements `RecipeExtractionService` port (renamed from `LangChain4jRecipeExtractionService`)
- `RecipeMapper`, `CategoryMapper` — entity↔domain mapping

### Entrypoint Layer (`entrypoint/rest/`)
- REST resources moved from `interfaceadapter.rest` to `entrypoint.rest`
- DTOs moved to `entrypoint.rest.dto`
- Controllers now delegate to use cases (no direct repository access)

### Removed
- `application.service` package (replaced by `domain` + `usecase`)
- `interfaceadapter.rest` package (replaced by `entrypoint.rest`)
- Old `infrastructure.persistence.repository` classes (replaced by adapter implementations)

## Frontend Changes

### New Layer Structure
- **`domain/model/`** — `Recipe.ts`, `Category.ts`, `RecipeImport.ts` with clean type definitions
- **`infrastructure/api/`** — `apiClient.ts` with all HTTP fetch logic
- **`application/`** — `useRecipes.ts`, `useCategories.ts`, `useRecipeImport.ts` hooks
- **`presentation/`** — all components, pages, App.tsx, and CSS
- **`shared/`** — `theme.css`

### Removed
- Old `api/client.ts` (replaced by `infrastructure/api/apiClient.ts` + `domain/model/`)
- Old `components/`, `pages/` top-level directories (moved under `presentation/`)
- Old `App.tsx`, `App.css`, `theme.css` at root level

## Test Coverage

- **Backend**: 1 new unit test class (`ConfirmRecipeImportTest`) for use case logic, plus
  `RecipeExtractionTest` (moved from `ExtractionResultValidationTest`). All existing integration
  tests updated to new package structure.
- **Frontend**: All 47 tests pass — moved to new locations with updated imports.

## How to Verify

```bash
# Frontend
cd frontend && npm test && npm run build

# Backend
./gradlew clean build
```

## Migration Notes

- No API contract changes — all REST endpoints remain identical
- No database changes
- No configuration changes
- Dependency direction: domain ← usecases ← infrastructure/entrypoint (strict inward)
