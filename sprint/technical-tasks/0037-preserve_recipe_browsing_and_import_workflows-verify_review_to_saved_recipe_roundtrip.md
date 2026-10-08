# Title
Verify extraction-to-confirmation persistence through the existing HTTP contracts

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md) — AC4, AC5, and AC7, with no changes to existing rows.

## Objective
Close the cross-layer regression gap between mocked UI tests, pure-core tests, and JDBC tests using one deterministic HTTP round-trip suite.

## Scope
A PostgreSQL-backed Quarkus test class exercising real REST mapping, use cases, JDBC persistence, and follow-up browsing with only extraction replaced by a deterministic test port.

## Out of Scope
Live AI, browser automation tooling, retries/idempotency features, new routes, replacement UI, repeated upload-boundary matrices already covered in 0021/0022, schema changes, and activating the quarantined legacy live-provider test.

## Clean Architecture Placement
Testing across outer REST/JDBC adapters and pure core. The AI substitute implements the inward extraction port; no test or framework dependency enters production domain/usecases.

## Execution Dependencies
- `0023-preserve_recipe_browsing_and_import_workflows-wire_recipe_confirmation_endpoint.md`
- `0024-preserve_recipe_browsing_and_import_workflows-convert_tests_to_jdbc_fixtures.md`
- `0026-preserve_recipe_browsing_and_import_workflows-postgresql_jdbc_preservation_integration.md`

## Implementation Details
- Add `RecipeImportWorkflowTest` with `@QuarkusTest`, `@Tag("integration")`, and the safe `PostgresIntegrationTestProfile` from 0026. Reuse its fresh, non-reusable PG17 resource and unchanged V1–V4 migrations. No localhost fallback, Flyway clean, fixed generated-ID expectation, or live model call.
- Extend the existing test-only `StubRecipeExtractionRepository` with an explicit test setter/reset for its `ExtractionResult`. Its default remains the fixed complete fixture needed by the bootstrap test. Reset in `@BeforeEach`/`@AfterEach`; run this class serially so scenarios cannot exchange mutable fixtures. Do not add test configuration switches to production code.
- Register `importWorkflowTest`, a Gradle `Test` task using the test source set/runtime and including tag `integration` and only the new workflow class. Follow 0026's runner bootstrap settings; ordinary `test` still excludes integration. Require nonzero discovered tests and separate reports. Do not include the disabled `RecipeImportIntegrationTest` or change `jdbcPreservationTest` filters.
- Scenario A: configure a complete English extraction with `200 g dark chocolate`, `4 eggs`, known title/preparation, warning, and stable raw source text. Capture JDBC recipe/category counts and historical rows, then POST a small supported PNG as multipart `file` to `/api/recipes/import`. Assert HTTP 200, `COMPLETE`, converted proposal, and unchanged stored rows/counts. GET `/api/recipes?categoryId=15` must not contain the proposal before confirmation.
- Submit that returned proposal unchanged in `POST /api/recipes/import/confirm` with null overrides. Capture the raw response and register the returned ID with `JdbcRecipeFixtures` before substantive assertions. Assert HTTP 201, category 15/Imported, zero views, exactly `7.1 oz (200 g) dark chocolate` (no second conversion), `4 eggs`, and original ingredient order. From an independent JDBC connection verify committed visibility and raw metadata. GET `/api/recipes/{id}` and category 15 listing must expose the same saved recipe. Repeated GET must not change views or historical rows.
- Scenario B: extract another complete proposal and confirm with title and preparation corrections, ingredient overrides `["200 g dark chocolate", "4 eggs"]`, and notes `Serve chilled.`. The frontend mapper's newline trimming is already tested in 0027; send its resulting array here rather than asking the HTTP backend to parse textarea text. Assert exact saved preparation `corrected preparation\n\nNotes: Serve chilled.`, converted ingredients, default Imported category, source metadata, and unchanged pre-existing recipes. Confirmed notes appear once.
- Scenario C (parameterized): provide an incomplete extraction with ingredients but no preparation, and an empty extraction. Both POSTs return 200 `NEEDS_MORE_INFO`, missing-field names and fixture warnings, editable proposal data, and zero writes. Supply valid title/ingredients/preparation overrides and confirm each; both produce independently committed details. No extraction response may itself contain a successful saved-recipe result. Do not assert that an AI stub proves actual model quality; English/no-invention prompt tests belong to 0018.
- Each test owns its inserted IDs. Cleanup recipes before categories in an unconditional teardown, even after failed assertions. Dispose the test container at suite end. Never clear shared seed data or change migration checksums. Existing REST/core tests remain responsible for validation and forced storage-failure matrices; existing frontend tests remain responsible for cancellation, error retention, and navigation.

## Files / Modules Impacted
- `backend/src/test/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportWorkflowTest.java`
- `backend/src/test/java/be/lutske/leolegacy/testsupport/StubRecipeExtractionRepository.java`
- `backend/src/test/java/be/lutske/leolegacy/testsupport/JdbcRecipeFixtures.java` (reuse; extend only if required for this suite's ownership registration).
- `backend/build.gradle.kts`

## Acceptance Criteria
- Given a complete, incomplete, or empty extraction, when upload completes without confirmation, then the response is a review proposal and the database is unchanged.
- Given valid confirmation, when HTTP 201 is returned, then the exact reviewed recipe is committed, queryable from Imported, and visible on detail retrieval with no added view-count behavior.
- Given a converted proposal or corrected metric ingredients, when confirmed, then conversion occurs once and optional notes follow the exact existing format.
- Given existing recipes, when the suite completes, then their stored content, category assignments, ingredients, preparation, and view counts are unchanged.

## Testing Requirements
Run `./gradlew :backend:importWorkflowTest -x :backend:buildFrontend -x :backend:copyFrontend` with the disposable container runtime and inspect the report for all four invocations (A, B, and two C cases). No AI credentials are needed. As the final story verification gate after all numbered tasks, run `./gradlew :core:domain:check :core:usecases:check :backend:check :backend:jdbcPreservationTest :backend:importWorkflowTest` from root, then `npm run test` and `npm run build` from `frontend`. Report infrastructure blockers rather than replacing integration tests with skips or targeting a local database. Full builds must preserve frontend packaging and version/SPA routes; those unrelated routes are not rewritten by this task.

## Dependencies / Preconditions
JDK 25, Node/npm for the final frontend gate, dependency/image download access, and a Docker or compatible Podman Testcontainers runtime. No user PostgreSQL database, persistent volumes, live AI service, or credentials may be used.
