## Summary
- Migrated the legacy static website to be served by Quarkus from `src/main/resources/META-INF/resources/`.
- Added a new backend API endpoint `GET /api/version`.
- Wired the homepage to fetch and display the API version using jQuery.

## User impact
- The site is now started with Quarkus instead of a Python static HTTP server.
- The homepage now shows the configured application version from the backend.

## Config/env changes
- Added `app.version` to `src/main/resources/application.properties`.

## Platform dependency notes
- Added Quarkus Maven project with `quarkus-resteasy-reactive`, `quarkus-resteasy-reactive-jackson`, and `quarkus-arc` dependencies.

## Migration notes
- Static assets were copied from `leo-legacy-static/leo-legacy.be/` into `src/main/resources/META-INF/resources/`.
- Existing static structure and relative paths were preserved.

## How to verify
1. `./mvnw quarkus:dev`
2. Open `http://localhost:8080/` and confirm homepage renders.
3. Confirm version appears on homepage (via `/api/version` call).
4. Open `http://localhost:8080/api/version` and verify JSON response.
5. `./mvnw test` then `./mvnw package` and run packaged mode:
   `java -jar target/quarkus-app/quarkus-run.jar`.
