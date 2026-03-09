# Serve Frontend from Quarkus on Port 8080

**Date:** 2026-03-09

## Summary

The React frontend is now automatically built and bundled into the Quarkus backend during `./gradlew :backend:build`. The full application (frontend + API) is served from a single Quarkus instance on port 8080.

## Changes

- **build.gradle.kts**: Added `buildFrontend` task (runs `npm install && npm run build`) and `copyFrontend` task (copies `frontend/dist/` into `META-INF/resources`)
- **SpaRoutingFilter.java**: New Vert.x router filter that reroutes non-API, non-file requests to `index.html` for React Router client-side routing
- **application.properties**: Added HTTP compression config
- **README.md**: Updated how-to-run docs to reflect single-port deployment

## User Impact

- `http://localhost:8080` now serves the full website (frontend + API)
- No need to run `npm run dev` separately for basic usage
- Vite dev server (port 3000) is still available for frontend hot-reload development

## How to Verify

```bash
podman compose up -d
./gradlew :backend:quarkusDev
# Open http://localhost:8080 — should show the React app
```
