# Title
Isolate upload validation and admit the exact 10 MiB multipart boundary

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — supported image formats and inclusive upload size; eventual story location.

## Objective
Keep transport/file validation in an outer validator and prevent multipart overhead from rejecting an otherwise valid maximum-size image.

## Scope
Add `RecipeImageUploadValidator`, delegate validation from the existing import resource, and adjust main/test Quarkus transport body limits.

## Out of Scope
Core file I/O, AI orchestration changes, persistence, image-content sniffing, new MIME/extension matching rules, UI changes, and schema changes.

## Clean Architecture Placement
Outer REST adapter `be.lutske.leolegacy.interfaceadapter.rest`, optionally `@ApplicationScoped`. It may know `FileUpload`, filesystem metadata, and `BadRequestException`; pure core accepts only bytes and MIME later.

## Execution Dependencies
None.

## Implementation Details
- Define `String validate(FileUpload file)` returning effective MIME after successful validation. Inject the validator into `RecipeImportResource` and replace its inline validator/constants. Preserve its remaining legacy extraction/confirmation paths until their own tasks; update constructor usages if any.
- Missing multipart `file` must produce explanatory HTTP 400 (`"File is required"`) rather than the current null dereference. A missing filename yields unsupported-extension 400, not a null dereference. The framework-provided upload path remains outer-only.
- Measure actual file size (`Files.size` or equivalent checked metadata access), reject only `size > 10L * 1024 * 1024`; exact 10,485,760 bytes is valid. Preserve the existing explanatory oversized message `"File too large: ... MB. Maximum is 10 MB."`.
- If present, MIME must be exactly one of `image/png`, `image/jpeg`, `image/jpg`, `image/webp`; null MIME is permitted and effective MIME becomes `image/png`. Empty/unsupported supplied MIME remains invalid. Independently validate case-insensitive final extension png/jpg/jpeg/webp using `Locale.ROOT`; do not infer MIME from extension or require matching pairs.
- Preserve unsupported MIME/extension messages identifying the rejected value and allowed formats. Do not introduce content decoding or reject zero bytes as a new rule. Resource remains responsible for reading validated bytes; file access/read errors map to HTTP 500 with the existing `Failed to read uploaded file: ...` error shape, never successful extraction.
- Change `quarkus.http.limits.max-body-size=10M` in **both** main and test properties to `12M` (12 MiB transport envelope). Application validator still enforces the exact 10 MiB file limit. A multipart request with normal headers/boundaries and a 10 MiB part must reach the endpoint; 10 MiB + 1 byte must also reach application validation and return 400. Bodies beyond the transport envelope may retain transport 413. Check other effective multipart limits only if the focused boundary test shows they block a valid file; do not replace the per-file limit with the request limit.

## Files / Modules Impacted
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImageUploadValidator.java`
- `backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`
- `backend/src/main/resources/application.properties`
- `backend/src/test/resources/application.properties`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImageUploadValidatorTest.java`
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResourceTest.java`

## Acceptance Criteria
- Given missing file, bad extension, or unsupported supplied MIME, when uploading, then explanatory 400 occurs before AI invocation or saving.
- Given a supported exact-10-MiB file in multipart, when uploaded, then it reaches extraction and is not rejected for size.
- Given 10 MiB + 1 byte, when uploaded within the transport envelope, then application validation rejects it with 400.

## Testing Requirements
Plain validator tests with temporary files/FileUpload doubles cover null file/name/MIME, every supported extension/MIME, uppercase extension, no extension, mismatched but independently allowed extension/MIME, exact size and one byte above. Add real multipart HTTP boundary tests to existing `RecipeImportResourceTest` using its mocked LangChain4j delegate; verify rejected inputs never invoke it. Use `@TempDir` cleanup, not permanent fixtures. Run `./gradlew :backend:test --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeImageUploadValidatorTest --tests be.lutske.leolegacy.interfaceadapter.rest.RecipeImportResourceTest -x :backend:buildFrontend -x :backend:copyFrontend`. These are boundary tests, not the reserved cross-layer workflow suite.

## Dependencies / Preconditions
JDK 25 and existing Quarkus/H2 test dependencies. No live AI credentials.
