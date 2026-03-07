# Staging Hygiene Pass

## Summary
- Rewrote `.gitignore` with comprehensive, well-organized ignore patterns covering OS files, IDE configs, AI tool configs, build artifacts, and environment secrets.
- Removed 4 accidentally tracked files from git: Eclipse `.project`, OpenCode config (`opencode.jsonc`), and two `.opencode/mode/*.md` files.
- Verified no other accidental files remain tracked.

## What Was Removed from Git Tracking
| File | Reason |
|---|---|
| `.project` | Eclipse IDE project file (developer-local) |
| `opencode.jsonc` | OpenCode AI tool config pointing to local Ollama instance |
| `.opencode/mode/software-developer-local.md` | AI tool mode config (already in `.gitignore` but was committed before the rule existed) |
| `.opencode/mode/software-developer.md` | AI tool mode config (same as above) |

These files remain on disk but are no longer tracked by git.

## .gitignore Additions
| Pattern | Purpose |
|---|---|
| `.DS_Store`, `Thumbs.db`, `ehthumbs.db`, `Desktop.ini` | OS-generated files |
| `.vscode/`, `*.iml`, `*.iws`, `*.ipr`, `.project`, `.classpath`, `.settings/` | IDE/editor configs |
| `*.swp`, `*.swo`, `*~` | Editor swap/backup files |
| `opencode.jsonc` | AI tool root config |
| `.kotlin/` | Kotlin compiler cache |
| `.quarkus/` | Quarkus dev mode artifacts |
| `.env`, `.env.*`, `!.env.example` | Environment secrets (with example exception) |

## How to Verify
1. `git ls-files | grep -iE '\.project|opencode|\.DS_Store|\.iml'` returns nothing.
2. `git ls-files --others --exclude-standard` returns nothing (no untracked files leaking through).
3. `./gradlew :backend:build` passes (all 14 backend tests green).
4. `cd frontend && npm test` passes (all 43 frontend tests green).
5. `cd frontend && npm run build` succeeds.
