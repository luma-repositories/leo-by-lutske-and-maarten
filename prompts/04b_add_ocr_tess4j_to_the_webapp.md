# Prompt
You are a senior full-stack engineer (Quarkus + PostgreSQL + React/Vite) and you will implement “recipe import from image” end-to-end.

Current state (assume from previous work):
- Backend: Quarkus REST API + PostgreSQL + Flyway migrations. There is an existing Recipe data model (or create/extend one).
- Frontend: React (Vite) app themed in Italian colors, consuming `/api/*` endpoints.

Goal:
Enable users to upload an image of a recipe (photo or screenshot). The UI uploads it to the backend. The backend uses **Tess4J** (Tesseract OCR) to extract text, tries to map it into the **Recipe** data model, and stores it in the database. If mapping is incomplete or ambiguous, the backend must return a structured “needs more info” response so the UI can prompt the user for missing fields via a text form, then finalize and store.

Functional requirements

A) Frontend (React)
1) Add a “Import Recipe” screen/component:
    - Image upload input (accept png/jpg/jpeg/webp).
    - Optional preview of the selected image.
    - A submit button that calls backend endpoint.
    - While processing: show loading state.
2) On success:
    - Show the parsed recipe summary (title, ingredients count, etc.).
    - Navigate to recipe detail page.
3) On “needs more info”:
    - Show the OCR’d raw text (read-only text area) so user can see what was extracted.
    - Show editable fields for the missing/uncertain parts (e.g., title, servings, ingredients, instructions, tags).
    - Include one additional free-form “Notes / corrections” text field.
    - Provide a “Confirm & Save” button that calls a second endpoint to finalize the recipe creation.
4) If OCR fails:
    - Show an error message and keep the user on the import screen.
5) Keep same-origin calls:
    - In dev, keep the Vite proxy so calls to `/api/...` work without CORS.

B) Backend (Quarkus)
1) Add an endpoint to accept the image upload:
    - `POST /api/recipes/import`
    - Content-Type: `multipart/form-data`
    - Field name: `file`
    - Response types:
        - 201 Created: JSON with created recipe DTO (id, title, etc.)
        - 422 Unprocessable Entity: JSON “needs more info” object including:
            - `status: "NEEDS_MORE_INFO"`
            - `rawText`: OCR output text
            - `proposedRecipe`: partial structured DTO (nullable fields allowed)
            - `missingFields`: list of strings (e.g., ["title","ingredients","instructions"])
            - `parseWarnings`: list of human-readable warnings
2) Add an endpoint to finalize when user provides missing info:
    - `POST /api/recipes/import/confirm`
    - Body JSON includes:
        - `rawText` (from previous response)
        - `proposedRecipe` (the partial DTO, possibly edited)
        - `userOverrides` (the extra form fields, including free-form notes)
    - Behavior:
        - Validate and produce a complete Recipe entity that fits the DB model.
        - Store it; return 201 with full recipe DTO.
3) OCR implementation using Tess4J:
    - Use Tess4J library (and include it in Maven dependencies).
    - Configure language(s): default to English; allow future extension to `eng+nld` but keep initial simple.
    - Consider basic pre-processing:
        - Convert to BufferedImage
        - Optionally grayscale / threshold if easy (only if it improves reliability without overcomplication)
4) Tesseract runtime strategy (choose one and document clearly in README):
   Option 1 (preferred for local dev): run tesseract as a container in podman compose and call it from backend (but requirement says Tess4J, so this is only acceptable if still using Tess4J and local tesseract binaries exist).
   Option 2: bundle/assume local tesseract installation and configure `TESSDATA_PREFIX`.
   Option 3: package tesseract + traineddata inside container image for backend (if backend is containerized).
   Pick the simplest that works with podman-compose local environment and document installation steps.

C) Recipe data model + parsing
1) Ensure Recipe model supports typical recipe structure:
    - id (UUID or long)
    - title (required)
    - description (optional)
    - servings (optional)
    - ingredients (required list) — either separate table or JSON column; follow existing approach
    - instructions/steps (required list or text)
    - source (optional, e.g., “Imported from image”)
    - createdAt
2) Implement a parsing service:
    - Input: raw OCR text
    - Output: ProposedRecipe (partial) + missingFields + warnings
    - Heuristics:
        - Title: first non-empty line, or line in ALL CAPS, or line before “Ingredients”
        - Ingredients: section between “Ingredients” and “Instructions/Directions/Method/Steps”
        - Instructions: section after “Instructions/Directions/Method/Steps”
        - If no headings are present, attempt:
            - Split by blank lines and detect ingredient-like lines (quantities/units)
            - Treat remaining text as steps
    - Keep the logic robust, not over-engineered; return NEEDS_MORE_INFO when uncertain.

D) Database + migrations
- Add any needed tables/columns for recipes and recipe items via Flyway migrations.
- If ingredients/steps are separate tables, create migrations accordingly.
- Ensure the seed data (if any) still works.

E) Security + limits
- Reject files larger than a reasonable size (e.g., 5–10 MB) with clear error.
- Validate MIME types and file extensions.
- Avoid storing the image itself unless required; only store extracted text + recipe data. (If you store images, do it intentionally and document.)

F) Output required
- Provide a step-by-step plan and then implement it.
- List all created/modified files and show exact contents/diffs for:
    - Backend: pom.xml, application.properties, REST resources, OCR service, parser, DTOs, entity changes, Flyway migrations
    - Frontend: new ImportRecipe page/components, API client methods, routing updates, minimal UI for “needs more info”
    - podman compose changes only if needed for tesseract runtime
    - README updates with run instructions and any prerequisites

Acceptance tests / manual test steps
1) Start DB: `podman compose up -d`
2) Start backend: `./mvnw quarkus:dev`
3) Start frontend: `cd frontend && npm run dev`
4) Upload a clear recipe image:
    - Verify recipe is created and appears in list/detail views.
5) Upload a messy image:
    - Verify backend returns NEEDS_MORE_INFO and UI shows the follow-up form.
    - Fill missing fields and confirm; recipe is created and persisted.
6) Restart services:
    - Verify recipe remains in DB.

Now proceed to implement end-to-end, keeping the solution minimal but production-leaning and consistent with the existing codebase structure.