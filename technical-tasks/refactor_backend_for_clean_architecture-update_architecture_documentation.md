# Update architecture documentation for clean architecture

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create comprehensive documentation that explains the new clean architecture structure, helping developers understand the organization, responsibilities, and patterns used in the refactored backend.

## Scope

- Create architecture overview documentation
- Document layer responsibilities and boundaries
- Create developer onboarding guide for the new structure
- Document coding patterns and conventions
- Create decision records for architectural choices
- Update existing README files with new structure information
- Create diagrams showing layer relationships and data flow

## Out of Scope

- API documentation updates (maintain existing)
- User-facing documentation changes
- Deployment documentation changes
- Performance optimization documentation

## Implementation Details

### Architecture Overview Documentation

Create `docs/architecture/clean-architecture-overview.md`:

**Content structure:**
```markdown
# Clean Architecture Overview

## Introduction
Explanation of clean architecture principles and why they were adopted.

## Layer Structure
- Domain Layer (Core Business Logic)
- Application Layer (Use Cases)
- Interface Adapter Layer (Controllers, DTOs)
- Infrastructure Layer (Database, External Services)

## Key Principles
- Dependency Inversion
- Separation of Concerns
- Testability
- Independence from Frameworks

## Benefits
- Easier maintenance
- Faster feature development
- Reduced coupling
- Improved testability
```

**Layer Responsibilities:**

**Domain Layer (`be.lutske.leolegacy.domain`):**
- Contains core business entities (Recipe, Category)
- Defines business rules and validation
- Contains domain repository interfaces
- Defines domain events
- No dependencies on other layers

**Application Layer (`be.lutske.leolegacy.application`):**
- Contains use cases (business workflows)
- Orchestrates domain operations
- Manages transaction boundaries
- Handles domain events
- Depends only on domain layer

**Interface Adapter Layer (`be.lutske.leolegacy.interfaceadapter`):**
- Contains REST controllers
- Handles request/response mapping
- Translates between external and internal formats
- Depends on application and domain layers

**Infrastructure Layer (`be.lutske.leolegacy.infrastructure`):**
- Contains database implementations
- Contains external service integrations
- Implements domain repository interfaces
- Handles technical concerns
- Depends on domain layer interfaces

### Developer Onboarding Guide

Create `docs/developer-guide/onboarding.md`:

**Content structure:**
```markdown
# Developer Onboarding Guide

## Understanding the Architecture

### Where to Find Things
- Business Logic: `domain/model/` and `application/usecase/`
- API Endpoints: `interfaceadapter/rest/`
- Database Access: `infrastructure/persistence/`
- Configuration: `infrastructure/config/`

### Adding New Features

#### 1. Start with Domain
- Define business entities in `domain/model/`
- Add business rules and validation
- Create domain repository interfaces

#### 2. Create Use Cases
- Implement business workflows in `application/usecase/`
- Define input/output DTOs
- Add transaction boundaries

#### 3. Add REST Endpoints
- Create controllers in `interfaceadapter/rest/`
- Map requests to use case commands
- Handle error responses

#### 4. Implement Infrastructure
- Create repository adapters in `infrastructure/persistence/`
- Implement entity mapping
- Handle technical concerns

### Testing Strategy
- Domain: Unit tests with no dependencies
- Use Cases: Integration tests with test doubles
- Controllers: Unit tests with mocked use cases
- Infrastructure: Integration tests with real database
```

### Coding Patterns and Conventions

Create `docs/developer-guide/coding-patterns.md`:

**Content structure:**
```markdown
# Coding Patterns and Conventions

## Domain Layer Patterns

### Entity Design
- Immutable value objects for IDs
- Business rule enforcement in entity methods
- Domain events for state changes
- No infrastructure dependencies

### Repository Interfaces
- Use domain entities as parameters and return types
- Define business-focused query methods
- Keep interfaces simple and focused

## Application Layer Patterns

### Use Case Design
- One use case per business operation
- Clear input/output boundaries with DTOs
- Transaction management at use case level
- Domain event publishing after successful operations

### Error Handling
- Use domain-specific exceptions
- Translate infrastructure exceptions
- Provide meaningful error messages

## Infrastructure Patterns

### Repository Implementation
- Implement domain repository interfaces
- Handle entity mapping between domain and JPA
- Translate exceptions to domain exceptions
- Optimize queries for performance

### Entity Mapping
- Separate domain entities from JPA entities
- Use mappers for conversion
- Handle complex relationships appropriately
- Preserve domain invariants during mapping
```

### Architecture Decision Records

Create `docs/architecture/decisions/`:

**ADR-001: Adopt Clean Architecture**
```markdown
# ADR-001: Adopt Clean Architecture

## Status
Accepted

## Context
The backend application needed better separation of concerns and improved maintainability.

## Decision
Implement Clean Architecture with clear layer separation and dependency inversion.

## Consequences
- Improved testability
- Better separation of business logic
- Easier to onboard new developers
- More complex initial setup
```

**ADR-002: Use Domain Events for Cross-Cutting Concerns**
```markdown
# ADR-002: Use Domain Events for Cross-Cutting Concerns

## Status
Accepted

## Context
Need to handle audit logging, notifications, and other cross-cutting concerns without coupling business logic.

## Decision
Implement domain events published from use cases and handled by dedicated event handlers.

## Consequences
- Decoupled business operations
- Extensible for future requirements
- Consistent audit trail
- Additional complexity in event handling
```

**ADR-003: Transaction Management at Use Case Level**
```markdown
# ADR-003: Transaction Management at Use Case Level

## Status
Accepted

## Context
Transaction boundaries were scattered across controllers, mixing infrastructure concerns with interface adapters.

## Decision
Move all transaction management to use case level with appropriate configuration.

## Consequences
- Clear transaction boundaries
- Proper separation of concerns
- Consistent rollback behavior
- Use cases become the natural transaction boundary
```

### Layer Interaction Diagrams

Create `docs/architecture/diagrams/`:

**layer-dependencies.md:**
```markdown
# Layer Dependencies

```
┌─────────────────────────────────────┐
│        Interface Adapters           │
│    (REST Controllers, DTOs)         │
└─────────────┬───────────────────────┘
              │ depends on
              ▼
┌─────────────────────────────────────┐
│         Application Layer           │
│      (Use Cases, Services)          │
└─────────────┬───────────────────────┘
              │ depends on
              ▼
┌─────────────────────────────────────┐
│          Domain Layer               │
│   (Entities, Repository Interfaces) │
└─────────────┬───────────────────────┘
              ▲ implements
              │
┌─────────────────────────────────────┐
│       Infrastructure Layer          │
│  (Database, External Services)      │
└─────────────────────────────────────┘
```

**recipe-import-flow.md:**
```markdown
# Recipe Import Flow

```
REST Controller
      │
      ▼
ImportRecipeFromImageUseCase
      │
      ├─► RecipeExtractionService (AI)
      │
      ├─► Recipe.create() (Domain)
      │
      ├─► RecipeRepository.save()
      │
      └─► DomainEventPublisher.publish()
                │
                ▼
          Event Handlers
          (Audit, Statistics)
```

### Package Structure Documentation

Create `docs/architecture/package-structure.md`:

**Content structure:**
```markdown
# Package Structure

## Overview
```
be.lutske.leolegacy/
├── domain/                    # Core business logic
│   ├── model/                # Business entities
│   ├── repository/           # Repository interfaces
│   ├── event/               # Domain events
│   └── exception/           # Domain exceptions
├── application/              # Application services
│   ├── usecase/             # Business use cases
│   ├── service/             # Domain services
│   └── event/               # Event handlers
├── interfaceadapter/         # External interfaces
│   └── rest/                # REST controllers
└── infrastructure/           # Technical implementation
    ├── persistence/         # Database implementation
    ├── ai/                  # AI service implementation
    └── config/              # Configuration
```

## Package Responsibilities

### Domain Package
- **Purpose**: Contains core business logic
- **Dependencies**: None (pure business logic)
- **Key Classes**: Recipe, Category, RecipeRepository interface

### Application Package
- **Purpose**: Orchestrates business operations
- **Dependencies**: Domain layer only
- **Key Classes**: CreateRecipeUseCase, ImportRecipeFromImageUseCase

### Interface Adapter Package
- **Purpose**: Handles external communication
- **Dependencies**: Application and Domain layers
- **Key Classes**: RecipeResource, CategoryResource

### Infrastructure Package
- **Purpose**: Implements technical concerns
- **Dependencies**: Domain interfaces
- **Key Classes**: JpaRecipeRepositoryAdapter, LangChain4jRecipeExtractionService
```

### Migration Guide

Create `docs/migration/clean-architecture-migration.md`:

**Content structure:**
```markdown
# Clean Architecture Migration Guide

## Migration Steps

### Phase 1: Domain Layer
1. Create domain entities
2. Define repository interfaces
3. Implement domain events
4. Add domain validation

### Phase 2: Application Layer
1. Create use cases
2. Move business logic from controllers
3. Add transaction management
4. Implement event publishing

### Phase 3: Infrastructure Layer
1. Create repository adapters
2. Implement entity mapping
3. Handle exception translation
4. Optimize data access

### Phase 4: Interface Adapters
1. Refactor controllers to use use cases
2. Remove business logic from controllers
3. Add proper error handling
4. Update request/response mapping

## Validation Steps

### After Each Phase
- Run all existing tests
- Verify application starts successfully
- Test existing user workflows
- Check performance metrics

### Final Validation
- All tests pass
- No regression in functionality
- Improved code organization
- Better separation of concerns
```

## Files / Modules Impacted

**New files to create:**
- `docs/architecture/clean-architecture-overview.md`
- `docs/architecture/package-structure.md`
- `docs/architecture/decisions/ADR-001-adopt-clean-architecture.md`
- `docs/architecture/decisions/ADR-002-domain-events.md`
- `docs/architecture/decisions/ADR-003-transaction-management.md`
- `docs/architecture/diagrams/layer-dependencies.md`
- `docs/architecture/diagrams/recipe-import-flow.md`
- `docs/developer-guide/onboarding.md`
- `docs/developer-guide/coding-patterns.md`
- `docs/migration/clean-architecture-migration.md`

**Files to update:**
- `README.md` (add links to new documentation)
- `docs/README.md` (update with new structure)

**Directory structure:**
```
docs/
├── architecture/
│   ├── clean-architecture-overview.md
│   ├── package-structure.md
│   ├── decisions/
│   │   ├── ADR-001-adopt-clean-architecture.md
│   │   ├── ADR-002-domain-events.md
│   │   └── ADR-003-transaction-management.md
│   └── diagrams/
│       ├── layer-dependencies.md
│       └── recipe-import-flow.md
├── developer-guide/
│   ├── onboarding.md
│   └── coding-patterns.md
├── migration/
│   └── clean-architecture-migration.md
└── README.md (updated)
```

## Acceptance Criteria

**Given** architecture documentation is created  
**When** a new developer joins the project  
**Then** they should be able to understand the backend structure within 2 hours

**Given** coding patterns documentation is available  
**When** a developer needs to add a new feature  
**Then** they should know where to place each component

**Given** migration guide is documented  
**When** the refactoring is performed  
**Then** each step should be clearly defined and verifiable

**Given** architecture decision records are created  
**When** architectural questions arise  
**Then** the reasoning behind decisions should be documented and accessible

**Given** package structure documentation exists  
**When** developers review the codebase  
**Then** they should understand the responsibility of each package

**Given** layer interaction diagrams are provided  
**When** developers need to understand data flow  
**Then** they should have visual representations of the architecture

**Given** all documentation is complete  
**When** it is reviewed for accuracy  
**Then** it should correctly reflect the implemented architecture

## Testing Requirements

**Documentation Tests:**

Create `DocumentationValidationTest.java`:
- Verify all referenced files exist
- Check that code examples in documentation compile
- Validate that package structure matches documentation
- Ensure all ADRs are properly formatted

**Link Validation:**

Create automated checks for:
- Internal documentation links
- Code references in documentation
- Diagram accuracy
- Package structure alignment

**Content Review:**

Create review checklist for:
- Technical accuracy
- Clarity for new developers
- Completeness of coverage
- Consistency with implementation

**Test Coverage Requirements:**
- All documented patterns must have corresponding code examples
- All architectural decisions must be reflected in the codebase
- Documentation must be validated against actual implementation
- Migration steps must be testable and verifiable