# Refactor RecipeResource to use application services

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Refactor the existing RecipeResource REST controller to use application services instead of directly accessing infrastructure repositories, following clean architecture principles.

## Scope

- Update RecipeResource to inject application services instead of repositories
- Replace direct repository calls with use case executions
- Update method implementations to use domain entities
- Maintain existing REST API contracts
- Remove direct infrastructure dependencies

## Out of Scope

- Changes to REST API contracts or endpoints
- Changes to DTO structures (handled in separate tasks)
- Database schema changes
- Frontend integration changes

## Clean Architecture Placement

- interface adapters

## Execution Dependencies

- 0015-refactor_backend_for_clean_architecture-create_get_recipe_use_case.md
- 0016-refactor_backend_for_clean_architecture-create_list_recipes_use_case.md
- 0017-refactor_backend_for_clean_architecture-create_list_recipes_by_category_use_case.md
- 0018-refactor_backend_for_clean_architecture-create_increment_recipe_view_count_use_case.md

## Implementation Details

Update RecipeResource to:
- Inject GetRecipeUseCase, ListRecipesUseCase, ListRecipesByCategoryUseCase, IncrementRecipeViewCountUseCase
- Remove direct RecipeRepository injection
- Replace repository.findById() calls with getRecipeUseCase.execute()
- Replace repository.findAll() calls with listRecipesUseCase.execute()
- Replace repository.findByCategory() calls with listRecipesByCategoryUseCase.execute()
- Add view count increment when retrieving individual recipes
- Update mapping from domain entities to DTOs
- Handle domain exceptions and map to appropriate HTTP responses

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeResource.java (modify existing)

## Acceptance Criteria

Given a GET request to /recipes/{id}
When the RecipeResource processes the request
Then it should use GetRecipeUseCase and IncrementRecipeViewCountUseCase

Given a GET request to /recipes
When the RecipeResource processes the request
Then it should use ListRecipesUseCase

Given a GET request to /recipes?category={categoryId}
When the RecipeResource processes the request
Then it should use ListRecipesByCategoryUseCase

Given the updated RecipeResource
When checking dependencies
Then it should not have direct repository dependencies

## Testing Requirements

- Unit tests for all endpoint methods with mocked use cases
- Integration tests to verify REST API behavior remains unchanged
- Tests for exception handling and HTTP response mapping

## Dependencies / Preconditions

- All required use cases must exist
- Domain entities and value objects must exist