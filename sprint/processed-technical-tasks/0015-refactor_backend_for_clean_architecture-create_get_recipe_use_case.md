# Create GetRecipeUseCase application service

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a GetRecipeUseCase application service that orchestrates the retrieval of a single recipe by ID, following clean architecture principles and encapsulating the business workflow.

## Scope

- Create GetRecipeUseCase application service
- Implement recipe retrieval by ID workflow
- Handle recipe not found scenarios
- Use domain repository interface
- Place in application layer following clean architecture principles

## Out of Scope

- Repository implementation (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database concerns (handled in infrastructure layer)

## Clean Architecture Placement

- usecases

## Execution Dependencies

- 0001-refactor_backend_for_clean_architecture-create_domain_recipe_id_value_object.md
- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0011-refactor_backend_for_clean_architecture-create_domain_recipe_repository_interface.md
- 0013-refactor_backend_for_clean_architecture-create_domain_recipe_not_found_exception.md

## Implementation Details

Create a GetRecipeUseCase that:
- Accepts RecipeRepository as constructor dependency
- Provides execute(RecipeId id) method returning Recipe
- Uses repository.findById() to retrieve recipe
- Throws RecipeNotFoundException when recipe not found
- Contains no framework dependencies (pure Java)
- Follows single responsibility principle

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/application/usecase/GetRecipeUseCase.java (new file)

## Acceptance Criteria

Given a valid RecipeId for an existing recipe
When executing GetRecipeUseCase
Then the Recipe should be returned successfully

Given a valid RecipeId for a non-existing recipe
When executing GetRecipeUseCase
Then a RecipeNotFoundException should be thrown

Given a GetRecipeUseCase
When checking dependencies
Then it should only depend on domain interfaces

## Testing Requirements

- Unit tests for successful recipe retrieval
- Unit tests for recipe not found scenario
- Unit tests with mocked repository
- Unit tests for constructor dependency injection

## Dependencies / Preconditions

- RecipeId value object must exist
- Recipe entity must exist
- RecipeRepository interface must exist
- RecipeNotFoundException must exist