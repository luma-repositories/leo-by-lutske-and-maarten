# leo-by-lutske-and-maarten

This repository now contains a Quarkus application that serves the legacy static website and exposes backend APIs from the same origin.

## Project Layout

- `src/main/resources/META-INF/resources/` static website files (served at `/`)
- `src/main/java/` Quarkus backend code
- `src/main/resources/application.properties` runtime configuration

## API

- `GET /api/version`
  - Response: `{ "version": "<configured value>" }`
  - Config key: `app.version`

## Run in Development

```bash
./mvnw quarkus:dev
```

Then open:

- `http://localhost:8080/`
- `http://localhost:8080/api/version`

## Run Packaged Mode

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Then open:

- `http://localhost:8080/`
- `http://localhost:8080/api/version`
