# Create ListRecipesByCategoryUseCase application service

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a ListRecipesByCategoryUseCase application service that orchestrates the retrieval of recipes filtered by category, following clean architecture principles.

## Scope

- Create ListRecipesByCategoryUseCase application service
- Implement recipe listing by category workflow
- Use domain repository interface
- Place in application layer following clean architecture principles

## Out of Scope

- Repository implementation (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database concerns (handled in infrastructure layer)
- Category validation (handled by domain layer)

## Clean Architecture Placement

- usecases

## Execution Dependencies

- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md
- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0011-refactor_backend_for_clean_architecture-create_domain_recipe_repository_interface.md

## Implementation Details

Create a ListRecipesByCategoryUseCase that:
- Accepts RecipeRepository as constructor dependency
- Provides execute(CategoryId categoryId) method returning List<Recipe>
- Uses repository.findByCategory() to retrieve recipes by category
- Returns empty list when no recipes exist for the category
- Contains no framework dependencies (pure Java)
- Follows single responsibility principle

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/application/usecase/ListRecipesByCategoryUseCase.java (new file)

## Acceptance Criteria

Given a valid CategoryId with existing recipes
When executing ListRecipesByCategoryUseCase
Then all recipes for that category should be returned

Given a valid CategoryId with no recipes
When executing ListRecipesByCategoryUseCase
Then an empty list should be returned

Given a ListRecipesByCategoryUseCase
When checking dependencies
Then it should only depend on domain interfaces

## Testing Requirements

- Unit tests for successful recipe listing by category
- Unit tests for empty recipe list scenario
- Unit tests with mocked repository
- Unit tests for constructor dependency injection

## Dependencies / Preconditions

- CategoryId value object must exist
- Recipe entity must exist
- RecipeRepository interface must exist