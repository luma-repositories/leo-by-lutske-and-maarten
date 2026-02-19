## Summary
- Renamed legacy static-site files to remove Windows-incompatible characters from filenames.
- Updated all internal links/forms/resources to point to the renamed files.
- Verified the site serves completely via `python3 -m http.server 8000`.

## User impact
- Users can now clone/check out the repository on Windows without filename errors caused by `?` in filenames.
- Legacy pages and navigation continue to work with the normalized filenames.

## Config/env changes
- None.

## Platform dependency notes
- No Gradle/Quarkus platform dependency changes.

## Migration notes
- Filename normalization applied in `leo-legacy-static/leo-legacy.be/`:
  - `.php?id=` -> `_id_`
  - `.php?` -> `_`
  - ` ` (space) -> `_`

## How to verify
1. `cd leo-legacy-static/leo-legacy.be`
2. `python3 -m http.server 8000`
3. Open `http://localhost:8000/index.html` and navigate through recipe/menu pages.
4. Confirm no broken links and no missing assets.
