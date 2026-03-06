## Summary
- Migrated the legacy static website from Python HTTP server to a Quarkus application.
- Static site files are now served by Quarkus from `src/main/resources/META-INF/resources/`.
- Added a REST API endpoint `GET /api/version` returning the application version as JSON.
- The homepage (`index.html`) now fetches and displays the version via jQuery AJAX.
- Set up Gradle build system with centralized dependency management in `platform/quarkus-platform.gradle`.

## User impact
- The website is now served by Quarkus at `http://localhost:8080/` instead of Python HTTP server.
- The footer of the homepage displays the application version, fetched from the backend API.
- All existing recipe pages, navigation, and assets continue to work unchanged.

## Config/env changes
- New property `app.version=1.2.3` in `src/main/resources/application.properties`.
- Java 21+ is now required to run the application.

## Platform dependency notes
- Created `platform/quarkus-platform.gradle` with centralized version management:
  - Quarkus BOM: 3.17.7
  - Kotlin: 2.0.21
  - REST Assured: 5.5.0
- All dependency versions are declared in the platform file; no inline versions in `build.gradle.kts`.

## Migration notes
- Static site files copied from `leo-legacy-static/leo-legacy.be/` to `src/main/resources/META-INF/resources/`.
- Original `leo-legacy-static/` directory preserved as reference.
- `index.html` (in resources) modified to include jQuery CDN and version display script.
- Stale `target/` and `bin/` directories from previous builds are now gitignored.

## How to verify
1. Run `./gradlew quarkusDev`
2. Open `http://localhost:8080/` — homepage should load with version displayed in footer.
3. Open `http://localhost:8080/api/version` — should return `{"version":"1.2.3"}`.
4. Run `./gradlew test` — all tests should pass.
