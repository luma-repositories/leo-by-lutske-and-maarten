# Create ListRecipesUseCase application service

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a ListRecipesUseCase application service that orchestrates the retrieval of all recipes, following clean architecture principles and encapsulating the business workflow.

## Scope

- Create ListRecipesUseCase application service
- Implement recipe listing workflow
- Use domain repository interface
- Place in application layer following clean architecture principles

## Out of Scope

- Repository implementation (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database concerns (handled in infrastructure layer)
- Pagination logic (can be added later)

## Clean Architecture Placement

- usecases

## Execution Dependencies

- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0011-refactor_backend_for_clean_architecture-create_domain_recipe_repository_interface.md

## Implementation Details

Create a ListRecipesUseCase that:
- Accepts RecipeRepository as constructor dependency
- Provides execute() method returning List<Recipe>
- Uses repository.findAll() to retrieve all recipes
- Returns empty list when no recipes exist
- Contains no framework dependencies (pure Java)
- Follows single responsibility principle

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/application/usecase/ListRecipesUseCase.java (new file)

## Acceptance Criteria

Given recipes exist in the repository
When executing ListRecipesUseCase
Then all recipes should be returned as a list

Given no recipes exist in the repository
When executing ListRecipesUseCase
Then an empty list should be returned

Given a ListRecipesUseCase
When checking dependencies
Then it should only depend on domain interfaces

## Testing Requirements

- Unit tests for successful recipe listing with multiple recipes
- Unit tests for empty recipe list scenario
- Unit tests with mocked repository
- Unit tests for constructor dependency injection

## Dependencies / Preconditions

- Recipe entity must exist
- RecipeRepository interface must exist