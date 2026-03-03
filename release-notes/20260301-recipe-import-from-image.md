## Summary
- Added end-to-end recipe import from image using Quarkus upload endpoints and Tess4J OCR.
- Added heuristic OCR parsing that either creates a recipe directly or returns `NEEDS_MORE_INFO` with structured follow-up payload.
- Added React import screen with upload, OCR review, editable missing fields, and confirm-and-save flow.

## User impact
- Users can now import recipes from screenshots/photos instead of manual entry.
- When OCR parsing is incomplete, users get guided correction fields and can still finalize creation.

## Config/env changes
- Added OCR/runtime settings in `application.properties`:
  - `app.import.max-file-size-bytes`
  - `app.import.allowed-mime-types`
  - `app.import.allowed-extensions`
  - `app.import.ocr.language`
  - `app.import.ocr.tessdata-path`
- Local prerequisite: install Tesseract and set `TESSDATA_PREFIX`.

## Platform dependency notes
- Added backend dependencies:
  - `quarkus-resteasy-reactive-multipart`
  - `net.sourceforge.tess4j:tess4j`

## Migration notes
- Added Flyway migration `V3__add_recipe_import_support.sql`:
  - recipe import metadata columns
  - `legacy_id` made nullable for imported recipes
  - ensured `Imported` category exists

## How to verify
1. `podman compose up -d`
2. `./mvnw clean test`
3. `cd frontend && npm test && npm run build`
4. `./mvnw quarkus:dev`
5. Open `/recipes/import`, upload clear and messy images, confirm creation and persistence.
