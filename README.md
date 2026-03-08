# leo-by-lutske-and-maarten

A Quarkus application serving the legacy leo-legacy.be recipe website as static assets, with a REST API backend.

## Prerequisites

- Java 25+
- No global Gradle installation needed (uses Gradle wrapper 9.1)

## Project Structure

```
├── build.gradle.kts                              # Root build file (thin)
├── settings.gradle.kts                           # Gradle settings (includes backend)
├── gradle.properties                             # Gradle/Quarkus properties
├── platform/
│   └── quarkus-platform.gradle                   # Centralized dependency versions
├── backend/
│   ├── build.gradle.kts                          # Backend build file (Quarkus)
│   └── src/
│       ├── main/
│       │   ├── kotlin/be/lutske/leolegacy/       # Kotlin backend sources
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── VersionResource.kt        # GET /api/version endpoint
│       │   │       └── VersionResponse.kt        # Response DTO
│       │   └── resources/
│       │       ├── application.properties        # Quarkus + app configuration
│       │       └── META-INF/resources/           # Static website (served at /)
│       │           ├── index.html                # Homepage
│       │           ├── leo-legacy.css            # Stylesheet
│       │           ├── images/                   # Site images
│       │           └── ...                       # Recipe pages, menus, etc.
│       └── test/
│           └── kotlin/be/lutske/leolegacy/       # Test sources
│               └── interfaceadapter/rest/
│                   ├── VersionResourceTest.kt    # API endpoint tests
│                   └── HomePageAvailabilityTest.kt # Static site serving tests
└── leo-legacy-static/                            # Original static site (reference)
```

## How to Run

### Development Mode (live reload)

```bash
./gradlew :backend:quarkusDev
```

Then open:
- Homepage: [http://localhost:8080/](http://localhost:8080/)
- Version API: [http://localhost:8080/api/version](http://localhost:8080/api/version)

### Build & Run Packaged

```bash
./gradlew :backend:build
java -jar backend/build/quarkus-app/quarkus-run.jar
```

### Run Tests Only

```bash
./gradlew :backend:test
```

## API Endpoints

| Method | Path           | Description                          |
|--------|----------------|--------------------------------------|
| GET    | `/`            | Homepage (static HTML)               |
| GET    | `/api/version` | Returns app version as JSON          |

### Example: GET /api/version

```json
{
  "version": "1.2.3"
}
```

The version value is configured in `backend/src/main/resources/application.properties` via the `app.version` property.

## Configuration

Key application properties (in `backend/src/main/resources/application.properties`):

| Property      | Default | Description                        |
|---------------|---------|------------------------------------|
| `app.version` | `1.2.3` | Application version shown on site  |
