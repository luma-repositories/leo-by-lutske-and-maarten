# Clean Architecture Refactor

## Summary

- refactored the backend recipe, category, and import flows into explicit domain, use case, port, infrastructure, and REST entrypoint layers
- refactored the frontend into domain, application, infrastructure, presentation, and shared layers while preserving routes and API contracts
- kept dependency version governance centralized in `platform/quarkus-platform.gradle` without adding new backend dependencies

## User impact

- no user-facing API or route changes
- recipe browsing, detail views, version display, and recipe import behavior remain the same
- more UI strings now go through shared i18n instead of being hardcoded in components

## Config / environment changes

- none

## Platform dependency notes

- no new backend dependencies were introduced
- backend dependency management remains centralized in `platform/quarkus-platform.gradle`

## Migration notes

- none for runtime consumers
- internal imports moved to the new clean-architecture-oriented folders on both backend and frontend

## How to verify

- run `./gradlew clean build`
- run `./gradlew test`
- run `cd frontend && npm run lint && npm run test && npm run build`
- open the app and verify category navigation, recipe detail rendering, and recipe import review/confirm flow
