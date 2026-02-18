## How to install Tesseract

Install Tesseract OCR on your platform and verify that it runs from the
command line. Make sure the Italian language data (`ita`) is installed.

------------------------------------------------------------------------

## Windows

Download the latest Windows installer from the [UB Mannheim Tesseract
builds repository](https://github.com/UB-Mannheim/tesseract/wiki) and run it.\
Ensure Italian language data is selected during installation.

Add `C:\Program Files\Tesseract-OCR` to your system `PATH` so
`tesseract` is available from the command line.

Verify installation:

`tesseract --version`\
`tesseract --list-langs`

You should see `ita` in the language list.

------------------------------------------------------------------------

## macOS

Install via Homebrew:

`brew install tesseract`

Homebrew usually includes common language packs. If needed, install
additional language data separately.

Verify installation:

`tesseract --version`\
`tesseract --list-langs`

Typical tessdata locations are under:

`/opt/homebrew/share/tessdata` (Apple Silicon)\
`/usr/local/share/tessdata` (Intel)

------------------------------------------------------------------------

## Linux (Debian / Ubuntu)

Install Tesseract:

`sudo apt install tesseract-ocr`

Install Italian language data:

`sudo apt install tesseract-ocr-ita`

Verify installation:

`tesseract --version`\
`tesseract --list-langs`

Common tessdata path:

`/usr/share/tesseract-ocr/5/tessdata`
