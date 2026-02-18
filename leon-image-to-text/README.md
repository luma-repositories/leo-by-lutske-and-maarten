# Italian OCR with Tess4J only

This sample reads Italian text from an image using Tess4J and the Italian language data (`ita.traineddata`). No Ollama or LangChain4j is required.

## Prerequisites
- JDK 25
- Maven
- Tesseract installed with `ita.traineddata` (point `TESSDATA_PREFIX` or pass a custom tessdata path)
- Put test images either on disk or in the classpath under `src/main/resources/images/` (packaged inside the jar).

## Build and run
```bash
cd leon-image-to-text
mvn clean install
```

Notes:
- If you omit `tessdataPath`, the app uses `TESSDATA_PREFIX` or a local `tessdata` folder.
- Place test images in `src/main/resources/images/` to bundle them, then pass the filename (e.g., `sample.jpg`). Filesystem paths still work if the file exists.


## How to install Tesseract
See [How to install Tesseract](How_to_install_tesseract.md)