# Create IncrementRecipeViewCountUseCase application service

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create an IncrementRecipeViewCountUseCase application service that orchestrates the business workflow of incrementing a recipe's view count when accessed.

## Scope

- Create IncrementRecipeViewCountUseCase application service
- Implement view count increment workflow
- Handle recipe retrieval, increment, and persistence
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

Create an IncrementRecipeViewCountUseCase that:
- Accepts RecipeRepository as constructor dependency
- Provides execute(RecipeId id) method returning Recipe
- Retrieves recipe using repository.findById()
- Throws RecipeNotFoundException when recipe not found
- Calls recipe.incrementViewCount() to get updated recipe
- Saves updated recipe using repository.save()
- Returns the updated recipe
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/application/usecase/IncrementRecipeViewCountUseCase.java (new file)

## Acceptance Criteria

Given a valid RecipeId for an existing recipe
When executing IncrementRecipeViewCountUseCase
Then the recipe view count should be incremented and the updated recipe returned

Given a valid RecipeId for a non-existing recipe
When executing IncrementRecipeViewCountUseCase
Then a RecipeNotFoundException should be thrown

Given an IncrementRecipeViewCountUseCase
When checking the workflow
Then it should retrieve, increment, save, and return the recipe

## Testing Requirements

- Unit tests for successful view count increment
- Unit tests for recipe not found scenario
- Unit tests with mocked repository
- Unit tests verifying save operation is called

## Dependencies / Preconditions

- RecipeId value object must exist
- Recipe entity must exist
- RecipeRepository interface must exist
- RecipeNotFoundException must exist