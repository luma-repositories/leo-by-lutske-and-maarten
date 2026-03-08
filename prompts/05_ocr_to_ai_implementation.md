# Prompt
You are a senior full-stack engineer and AI integration architect. I have a Quarkus + PostgreSQL + React/Vite application with a Recipe domain model and an “Import Recipe” UI flow. I now want to move away from Tess4J/OCR-first parsing and instead use **LangChain4j** with a multimodal-capable LLM to extract recipe data directly from an uploaded image.

The solution must support at least:
- OpenAI models
- Claude models
- Local/self-hosted models exposed through **vLLM** with an OpenAI-compatible API

Goal:
A user uploads an image of a recipe in the UI. The backend sends that image to an LLM through LangChain4j, asks the model to extract a structured recipe, validates/massages the result into the Recipe data model, and stores it in PostgreSQL. If the result is incomplete, ambiguous, or does not fit the Recipe model, the backend must return a structured “needs more info” response so the UI can prompt the user for missing fields or corrections.

Important design goals:
1) Do NOT use Tess4J anymore.
2) Use LangChain4j as the abstraction layer for model providers.
3) The provider must be configurable so we can switch between OpenAI, Claude, and local vLLM without changing business logic.
4) The architecture should be extensible for future providers.
5) Keep the implementation practical and production-leaning, not a toy demo.

High-level functional requirements

A) Frontend (React / Vite)
1) Keep or create an “Import Recipe” page:
    - File upload input for image files (png/jpg/jpeg/webp).
    - Optional preview.
    - Submit button.
    - Loading/progress state while backend processes the image.
2) Success flow:
    - Show a recipe summary.
    - Navigate to recipe detail page after successful save.
3) Needs-more-info flow:
    - Show the model’s extracted raw text/notes if available.
    - Show the proposed structured recipe in editable form.
    - Highlight missing/uncertain fields.
    - Provide a free-form “Notes / corrections” input.
    - Confirm & Save button calls a follow-up endpoint.
4) Failure flow:
    - Show a clear error message if extraction fails or provider is unavailable.

B) Backend (Quarkus + LangChain4j)
Implement a provider-agnostic image-to-recipe pipeline.

1) Add upload endpoint:
- `POST /api/recipes/import`
- Accept `multipart/form-data`
- Field: `file`
- Behavior:
    - Validate file type and size
    - Pass image to an LLM extraction service
    - Parse/validate returned structured output
    - Either store recipe directly or return a “needs more info” response

2) Add confirm/finalize endpoint:
- `POST /api/recipes/import/confirm`
- Accept JSON body with:
    - `proposedRecipe`
    - `userOverrides`
    - `rawModelResponse` or `extractionContext` if needed
    - `notes`
- Behavior:
    - Merge user-provided corrections
    - Validate against Recipe model
    - Store in DB
    - Return 201 with created recipe DTO

3) Response contracts:
- Success:
    - 201 Created with recipe DTO
- Incomplete extraction:
    - 422 Unprocessable Entity with JSON:
        - `status: "NEEDS_MORE_INFO"`
        - `proposedRecipe`: partial recipe DTO
        - `missingFields`: array of field names
        - `warnings`: array of human-readable strings
        - `rawModelResponse`: optional sanitized model output for debugging/UI support
- Provider/model failure:
    - 502 or 500 with clear structured error

C) LLM extraction architecture
Build a clean abstraction so provider switching is configuration-only.

1) Create an interface such as:
- `RecipeImageExtractionService`
  with a method like:
- `ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType)`

2) Provide a LangChain4j-based implementation that:
- Supports OpenAI
- Supports Claude
- Supports local vLLM endpoint using OpenAI-compatible API
- Selects provider based on configuration in `application.properties` or profile-specific config

3) Suggested config model:
- `app.ai.provider=openai|claude|vllm`
- `app.ai.model=<model-name>`
- `app.ai.base-url=<url for vllm or compatible endpoint>`
- `app.ai.api-key=<secret or env-backed>`
- `app.ai.timeout=...`
- `app.ai.temperature=...` (likely low)
- `app.ai.max-tokens=...`

4) Prompting strategy:
   Use a strong extraction prompt that tells the model:
- It receives an image of a recipe
- It must extract and normalize into a strict JSON structure
- It must avoid hallucinating when data is missing
- It must explicitly leave fields null/empty if uncertain
- It must return warnings for uncertainty
- It must preserve ingredient quantities and cooking steps faithfully where possible

5) Structured output:
   Prefer schema-constrained JSON output if supported by the provider through LangChain4j.
   Design a DTO such as:
- title
- description
- servings
- ingredients: array of objects with amount, unit, item, notes
- steps: array of strings
- source
- tags
- warnings

If a provider does not reliably support strict structured output, implement a fallback:
- ask for JSON only
- parse defensively
- validate with backend DTO validation

D) Multimodal handling
The implementation must support sending an image to the model.

1) Use LangChain4j capabilities for multimodal/image input where supported.
2) For providers with different image input formats:
- encapsulate provider-specific differences in the adapter/service layer
- do not leak provider-specific code into REST resources or domain services
3) For local vLLM:
- assume an OpenAI-compatible chat/completions API with a multimodal-capable model
- document any required model assumptions
- example: a vision-capable model served behind vLLM
- if the configured model does not support images, fail clearly

E) Recipe domain model validation
Ensure the resulting structured data fits the Recipe model.

Recipe should support at least:
- id
- title (required)
- description (optional)
- servings (optional)
- ingredients (required)
- steps/instructions (required)
- source
- createdAt

Validation rules:
- title required
- at least one ingredient
- at least one step
- if these are missing, return NEEDS_MORE_INFO instead of silently inventing data

F) Database and persistence
1) Reuse or extend the existing Recipe tables/entities.
2) Add migrations if needed.
3) Store:
- final recipe data
- optionally store extraction metadata in a separate table or JSON column:
    - provider
    - model
    - created timestamp
    - warnings
    - raw model response or normalized extraction result
      Only do this if it adds value and is easy to maintain.

G) Provider support details
Implement at least these providers:

1) OpenAI
- configurable API key
- configurable model
- multimodal image input support

2) Claude
- configurable API key
- configurable model
- multimodal image input support

3) vLLM local
- configurable base URL
- configurable model
- assume OpenAI-compatible API
- document that the served model must be vision-capable

The application must make it easy to switch between them via config only, for example:
- `%dev.app.ai.provider=vllm`
- `%prod.app.ai.provider=openai`

H) Operational concerns
1) File validation:
- allowed mime types: png/jpg/jpeg/webp
- reasonable max size, e.g. 5–10 MB
2) Timeouts and retries:
- configure reasonable request timeout
- retry only where safe
3) Logging:
- log provider/model used
- do not log secrets
- avoid logging sensitive image contents
4) Error messages:
- clear distinction between:
    - invalid upload
    - model/provider unavailable
    - extraction incomplete
    - persistence error

I) Frontend UX details
For the “needs more info” form:
- Pre-fill extracted title, ingredients, steps, servings, description
- Let user edit ingredients and steps in a practical way
- Show warnings from backend
- Show a general notes/corrections field
- Make it easy to confirm and save

J) Testing
Add tests at least for:
1) Backend unit tests
- extraction result validation
- missing fields detection
- mapping DTO to entity
2) Backend integration tests
- `POST /api/recipes/import` with mocked extraction service
- `POST /api/recipes/import/confirm`
3) Frontend tests if already present
- import page renders
- needs-more-info flow renders returned fields

K) Output required from you
Provide:
1) A short architecture summary
2) A step-by-step implementation plan
3) All created/modified files
4) Exact contents or diffs for key files, including:
- `pom.xml`
- `application.properties`
- provider abstraction interfaces
- LangChain4j service implementation(s)
- provider configuration classes
- REST resources
- DTOs
- validation logic
- entity/repository/service changes
- Flyway migrations if needed
- React upload page and confirm form
- API client methods
- README updates

L) Recommended implementation style
- Backend: Quarkus + LangChain4j + PostgreSQL
- Frontend: React + Vite
- Keep API paths under `/api`
- Keep the code cleanly layered:
    - resource/controller
    - application service
    - AI/provider adapter
    - domain/persistence
- Do not hardcode provider-specific behavior all over the codebase

M) Acceptance criteria
1) I can configure `app.ai.provider=openai` and import a recipe image successfully.
2) I can configure `app.ai.provider=claude` and use the same UI/backend flow successfully.
3) I can configure `app.ai.provider=vllm` with a local OpenAI-compatible endpoint and import successfully, provided the model supports vision.
4) If the model output is incomplete, the UI shows a correction form and I can finalize the recipe.
5) Starting the app and importing recipes works consistently with the existing Quarkus + Postgres + React setup.

Now implement this end-to-end in the repository. Start by:
1) Inspecting the current Recipe model and existing import flow.
2) Introducing the provider-agnostic LangChain4j extraction abstraction.
3) Wiring OpenAI, Claude, and vLLM support through configuration.
4) Updating the import/finalize UI flow.
5) Documenting setup and example configurations for each provider.