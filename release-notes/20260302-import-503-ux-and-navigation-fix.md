## Summary
- Improved recipe import UX when OCR runtime is unavailable by surfacing backend error messages in the import screen.
- Added explicit navigation from import screen back to recipes overview.
- Hardened upload MIME validation to allow extension-only validation when multipart content type is missing.

## User impact
- Users now see actionable OCR runtime errors instead of a generic import failure message.
- Users can return to the recipe list directly from the import page.

## Config/env changes
- No new required configuration.

## Platform dependency notes
- No dependency changes.

## Migration notes
- No database migration required.

## How to verify
1. Start backend and frontend in dev mode.
2. Open `/recipes/import` and click back link to return to `/`.
3. Trigger OCR runtime error and confirm detailed backend message is shown.
4. Run `npm test`, `npm run build`, and `./mvnw test`.
