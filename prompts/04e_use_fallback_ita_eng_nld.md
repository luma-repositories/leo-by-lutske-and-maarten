# Prompt

Implement Tesseract OCR language support with fallback logic in the backend.

Setup:
- Use traineddata files from:
  https://github.com/tesseract-ocr/tessdata_best
- Download and place in:
  backend/tessdata
    - ita.traineddata
    - eng.traineddata
    - nld.traineddata

Configuration:
- Add configuration properties:
    - ocr.tessdata-path (default: ./tessdata)
    - ocr.language (default: ita)

Core behavior:
- Default OCR language is Italian (ita)
- The system must support overriding the language via configuration (e.g. ita+eng, eng, nld)
- Implement fallback logic when no language is explicitly provided:
    1. Try OCR with Italian (ita)
    2. If no Italian words are detected, retry with English (eng)
    3. If that also fails, retry with Dutch (nld)

Fallback definition:
- “No Italian words detected” means:
    - OCR result is empty, OR
    - OCR result does not contain recognizable words for that language (simple heuristic is fine, e.g. minimal word count or dictionary-like check)

Implementation details:
- Use Tess4J/Tesseract with a filesystem datapath (not classpath)
- Keep the implementation simple and readable
- Avoid heavy NLP; a lightweight heuristic is sufficient

Testing:
- Add tests that verify:
    - default language is Italian
    - fallback to English when Italian yields no meaningful result
    - fallback to Dutch when both Italian and English fail
    - custom language overrides bypass fallback logic
    - tessdata path is correctly read from configuration

Notes:
- Do not rely on src/main/resources at runtime unless files are extracted to disk
- Keep everything production-friendly and easy to configure