# leo-by-lutske-and-maarten

A Quarkus application serving the legacy leo-legacy.be recipe website as static assets, with a REST API backend.

## Prerequisites

- **Java 25** (OpenJDK 25) — see [Installing Java 25 with SDKMAN](#installing-java-25-with-sdkman) below
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
openjdk version "25" 2025-09-16
OpenJDK Runtime Environment (build 25+...)
OpenJDK 64-Bit Server VM (build 25+..., mixed mode, sharing)
```

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
│       │   ├── java/be/lutske/leolegacy/         # Java backend sources
│       │   │   └── interfaceadapter/rest/
│       │   │       ├── VersionResource.java      # GET /api/version endpoint
│       │   │       └── VersionResponse.java      # Response DTO (Java record)
│       │   └── resources/
│       │       ├── application.properties        # Quarkus + app configuration
│       │       └── META-INF/resources/           # Static website (served at /)
│       │           ├── index.html                # Homepage
│       │           ├── leo-legacy.css            # Stylesheet
│       │           ├── images/                   # Site images
│       │           └── ...                       # Recipe pages, menus, etc.
│       └── test/
│           └── java/be/lutske/leolegacy/         # Test sources
│               └── interfaceadapter/rest/
│                   ├── VersionResourceTest.java    # API endpoint tests
│                   └── HomePageAvailabilityTest.java # Static site serving tests
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
