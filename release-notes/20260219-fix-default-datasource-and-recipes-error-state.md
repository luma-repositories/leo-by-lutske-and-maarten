## Summary
- Hardened Quarkus datasource profile configuration for local development startup.
- Improved frontend recipes list behavior to show a clear API load error instead of an empty-data message when backend/data access fails.
- Added retry support for recipe list loading.

## User impact
- Users now get an actionable error message when recipes cannot be loaded.
- A retry button is available to recover after transient failures.

## Config/env changes
- Added explicit `%dev` datasource values in `application.properties` for stable local startup.
- Added explicit `%prod` datasource values to avoid missing-default-datasource startup failures under profile overrides.
- Added troubleshooting guidance for conflicting `QUARKUS_DATASOURCE_*`/`QUARKUS_PROFILE` environment variables.

## Platform dependency notes
- No backend dependency changes.
- Frontend test stack reused from existing setup.

## Migration notes
- No DB schema or API contract migration needed.

## How to verify
1. `podman compose up -d postgres`
2. `./mvnw clean test`
3. `cd frontend && npm test && npm run build`
4. `./mvnw quarkus:dev`
5. Stop backend or DB and verify UI shows load error with retry button.
