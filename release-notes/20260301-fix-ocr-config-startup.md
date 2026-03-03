## Summary
- Fixed Quarkus startup failures caused by required OCR config injection for optional properties.
- Switched OCR config lookup to optional runtime reads with safe defaults.

## User impact
- App now starts even when `app.import.ocr.tessdata-path` or `app.import.ocr.stub-text` are not defined.
- OCR import behavior remains unchanged when properties are provided.

## Config/env changes
- Added `app.import.ocr.stub-text=${APP_IMPORT_OCR_STUB_TEXT:}` to base config.
- `TESSDATA_PREFIX` remains optional; when unset, OCR adapter uses fallback logic.

## Platform dependency notes
- No dependency changes.

## Migration notes
- No DB/API migration required.

## How to verify
1. Ensure OCR env vars are unset.
2. Run `./mvnw clean test`.
3. Run `./mvnw quarkus:dev` and confirm startup succeeds.
