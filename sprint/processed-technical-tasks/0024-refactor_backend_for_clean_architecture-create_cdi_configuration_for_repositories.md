# Create CDI configuration for repository implementations

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create CDI configuration to bind domain repository interfaces to their infrastructure implementations, enabling dependency injection in application services.

## Scope

- Create CDI producer methods or configuration
- Bind RecipeRepository interface to JpaRecipeRepository implementation
- Bind CategoryRepository interface to JpaCategoryRepository implementation
- Ensure proper application scoped lifecycle
- Use Quarkus CDI features

## Out of Scope

- Changes to domain interfaces
- Changes to infrastructure implementations
- Application service modifications

## Clean Architecture Placement

- infrastructure

## Execution Dependencies

- 0020-refactor_backend_for_clean_architecture-create_jpa_recipe_repository_implementation.md
- 0021-refactor_backend_for_clean_architecture-create_jpa_category_repository_implementation.md

## Implementation Details

Create CDI configuration that:
- Uses @Produces annotation to create repository beans
- Binds domain interfaces to infrastructure implementations
- Ensures @ApplicationScoped lifecycle for repositories
- Follows Quarkus CDI best practices
- Enables injection of domain interfaces in application services

Options:
1. Create @Produces methods in a configuration class
2. Use @ApplicationScoped directly on implementation classes
3. Create explicit CDI configuration

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/infrastructure/config/RepositoryConfiguration.java (new file, if using producer approach)
- OR modify existing repository implementation classes with proper annotations

## Acceptance Criteria

Given application services that inject domain repository interfaces
When the application starts
Then CDI should successfully inject the infrastructure implementations

Given the CDI configuration
When checking bean scopes
Then repositories should be application scoped

Given the application startup
When CDI initializes
Then no dependency injection errors should occur

## Testing Requirements

- Integration tests for CDI configuration
- Tests for successful dependency injection
- Application startup tests

## Dependencies / Preconditions

- JpaRecipeRepository implementation must exist
- JpaCategoryRepository implementation must exist
- Domain repository interfaces must exist