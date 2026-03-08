# Add .env file for API key management

**Date:** 2026-03-08

## Summary

Added a `.env` file (gitignored) and `.env.example` template for storing API keys locally. Quarkus reads `.env` automatically via SmallRye Config — no extra dependencies needed.

## User Impact

- Developers copy `.env.example` to `.env` and set `AI_API_KEY` once, instead of exporting environment variables each session.
- The `.env` file is gitignored, so secrets are never committed.

## How to Verify

```bash
cp backend/.env.example backend/.env
# Set AI_API_KEY in backend/.env
./gradlew :backend:quarkusDev
# The app reads AI_API_KEY from backend/.env automatically
```

**Note:** The `.env` file lives in `backend/` because Quarkus `quarkusDev` runs from the module directory. SmallRye Config reads `.env` from the current working directory.

## Files Changed

- `backend/.env` — Local secrets file (gitignored)
- `backend/.env.example` — Committed template
- `.gitignore` — Updated `!.env.example` → `!**/.env.example` to whitelist in subdirectories
- `README.md` — Updated setup instructions to reference `backend/.env`
