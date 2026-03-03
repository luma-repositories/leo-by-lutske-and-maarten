## Summary
- Fixed OCR upload failure path when Tess4J cannot load native `libtesseract`.
- Added controlled error mapping so upload returns a clear service-unavailable OCR runtime message instead of an unhandled server error.

## User impact
- Recipe import now fails gracefully when native OCR libraries are missing.
- Error response clearly explains that native tesseract installation/runtime path must be fixed.

## Config/env changes
- No mandatory new config keys.
- Documented macOS runtime requirement for `DYLD_LIBRARY_PATH` and `TESSDATA_PREFIX`.

## Platform dependency notes
- No dependency changes.

## Migration notes
- No DB or API schema migration required.

## How to verify
1. Run `./mvnw clean test`.
2. Start backend without native tesseract path and verify import returns OCR runtime unavailable.
3. Export `DYLD_LIBRARY_PATH` and `TESSDATA_PREFIX`, restart backend, and retry import.
