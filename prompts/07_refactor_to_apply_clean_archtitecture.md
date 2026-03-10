# Prompt

You are a senior software architect and hands-on refactoring engineer.

Your task is to refactor an existing full-stack application (backend + frontend) toward Clean Architecture principles.

You must use the following architectural interpretation as the target model:

1. Core first
- The core contains the business domain and use cases.
- Core business logic must be independent from frameworks, databases, UI libraries, HTTP, file systems, and external services.
- The core may define interfaces/ports for persistence, external APIs, messaging, file access, auth/context, etc., but it must not contain implementation details.

2. Domain and use cases are distinct
- Domain contains the business models, value objects, rules, and language of the problem space.
- Use cases encapsulate business actions and application-specific workflows.
- Prefer use-case-oriented classes over generic “service” classes.
- Organize business logic around user actions / business capabilities to reduce regression risk and avoid large god services.

3. Infrastructure is outside the core
- All technical integrations belong outside the core.
- Database implementations, REST clients, GraphQL clients, file access, message brokers, cache integrations, framework adapters, etc. must live in infrastructure modules.
- Infrastructure implements the interfaces defined by the core.

4. Entrypoints / configuration stay outside infrastructure
- Controllers, REST endpoints, GraphQL resolvers, server wiring, framework bootstrapping, dependency injection setup, app startup, routing, and runtime-specific configuration belong in outer layers.
- These must not pollute the core.
- The core must remain unaware of the chosen backend or frontend framework.

5. Dependency direction is strict
- Changes may propagate from inner layers to outer layers, never the reverse.
- Infrastructure or UI changes must never force changes in the domain model or use cases unless the business itself changed.

6. Modularize by responsibility and replaceability
- Group modules by architectural responsibility and by replaceable technology.
- Example thinking:
    - domain
    - usecases
    - infrastructure-persistence-postgres
    - infrastructure-persistence-inmemory
    - infrastructure-external-rest
    - app-rest-spring / app-rest-quarkus / app-web-react / app-web-next
- Keep options open for replacing frameworks or deployment patterns later.

7. Avoid fake abstraction and accidental reuse
- Do not collapse everything into generic services just to remove a few repeated lines.
- Treat duplicated code carefully: if behaviors may diverge later, keep use cases separate.
- Prefer explicit use cases over over-generalized abstractions.

8. Backend and frontend must both be refactored
- Backend should follow Clean Architecture with clear ports/adapters boundaries.
- Frontend should also move toward a layered structure:
    - domain: frontend business models, UI-agnostic rules, type definitions
    - application/use-cases: orchestration, state transition logic, commands/queries, validation workflows
    - infrastructure: API clients, local storage, browser APIs, framework-specific adapters
    - presentation: pages, components, hooks/controllers/view models, routing, styling
- Presentation must not contain core business rules.
- API details should be isolated from reusable business logic.

Your working style:
- First inspect the existing project structure carefully.
- Infer the current architecture, anti-patterns, coupling, and boundaries.
- Then propose and apply a target structure.
- Make changes incrementally but coherently.
- Preserve behavior unless a change is necessary to remove architectural violations.
- Do not do a cosmetic rewrite only. Perform a real structural refactor.

Deliverables required:

A. Architecture assessment
Start by producing:
1. A concise assessment of the current backend architecture
2. A concise assessment of the current frontend architecture
3. The main Clean Architecture violations
4. The highest-risk coupling points
5. The recommended target module/package/folder structure

B. Target structure
Design a concrete target structure for this repository.
Use the existing stack where practical, but reorganize code into clean boundaries.

Backend target example (adapt to the real codebase):
- backend/domain
- backend/usecases
- backend/ports
- backend/infrastructure/persistence/<tech>
- backend/infrastructure/external/<tech>
- backend/infrastructure/messaging/<tech>
- backend/configuration or backend/entrypoints/<framework>

Frontend target example (adapt to the real codebase):
- frontend/src/domain
- frontend/src/application
- frontend/src/infrastructure
- frontend/src/presentation
- frontend/src/shared

C. Refactoring rules
Apply these rules:
- Move entities/value objects/business rules into domain
- Move application orchestration into use cases
- Introduce ports/interfaces where infrastructure is directly referenced from business logic
- Move DB/API/framework/browser code into adapters/infrastructure
- Move controllers/routes/components/pages into entrypoints/presentation
- Remove direct imports from core to frameworks
- Reduce bidirectional dependencies
- Replace “service soup” with use-case-driven orchestration where needed
- Keep DTOs and transport models separate from domain models when useful
- Add mappers where necessary
- For frontend, separate remote DTOs, UI models, and domain models when they differ
- Extract API calling logic from components/hooks into infrastructure/application layers
- Keep forms/components as thin as possible

D. Execution mode
Perform the work in this order:
1. Analyze current structure
2. Propose target structure
3. Refactor backend
4. Refactor frontend
5. Update imports/build wiring
6. Run/build/tests and fix issues
7. Summarize all changes

E. Output format
Use this exact structure in your response:

## 1. Current architecture assessment
## 2. Proposed target structure
## 3. Refactoring plan
## 4. Concrete code/file changes
## 5. Final dependency rules
## 6. Remaining technical debt
## 7. Validation results

F. Constraints
- Do not introduce unnecessary libraries.
- Do not over-engineer.
- Keep naming explicit and business-oriented.
- Preserve existing behavior and APIs wherever possible.
- Prefer small, obvious adapters over clever abstractions.
- If the codebase is too large for a full rewrite in one pass, refactor the most important slices first and clearly identify what remains.
- Where needed, create transitional adapters to keep the app running.
- Ensure the new structure is practical for future extraction of modules, microservices, or alternative runtimes/frameworks.

G. Specific things to look for
Backend:
- controllers calling repositories directly
- services mixing business logic and persistence
- framework annotations leaking into domain
- entities tightly coupled to ORM/web framework
- shared utility classes hiding business logic
- direct HTTP/database/file/auth calls in use case logic

Frontend:
- pages/components performing business decisions
- hooks mixing API access, state orchestration, and rendering concerns
- direct fetch/axios logic spread across components
- duplicated validation/business rules in components
- framework-specific state containers leaking everywhere
- transport DTOs being used directly as domain models

H. Refactoring objective
The end state should make it easy to:
- test business logic without infrastructure
- replace persistence/API implementations
- evolve backend and frontend independently
- change frameworks with limited impact
- reduce regression risk by isolating business behavior into explicit use cases

Now inspect the repository and start with:
1. a current-state architecture assessment
2. a proposed target structure
3. then the actual refactor