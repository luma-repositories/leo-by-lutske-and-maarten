# Update RecipeImportResource for clean architecture

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Update the existing RecipeImportResource to follow clean architecture principles by using application services and domain entities instead of direct infrastructure access.

## Scope

- Review current RecipeImportResource implementation
- Create necessary application services for recipe import workflow
- Update RecipeImportResource to use application services
- Maintain existing REST API contracts
- Remove direct infrastructure dependencies

## Out of Scope

- Changes to REST API contracts or endpoints
- Changes to AI extraction service interface (already clean)
- Database schema changes
- Frontend integration changes

## Clean Architecture Placement

- interface adapters

## Execution Dependencies

- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0020-refactor_backend_for_clean_architecture-create_jpa_recipe_repository_implementation.md

## Implementation Details

Update RecipeImportResource to:
- Create ImportRecipeUseCase application service if needed
- Use application services instead of direct repository access
- Map between DTOs and domain entities properly
- Handle domain exceptions and map to appropriate HTTP responses
- Maintain existing import workflow behavior
- Use RecipeExtractionService through application layer

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java (modify existing)
- backend/src/main/java/be/lutske/leolegacy/application/usecase/ImportRecipeUseCase.java (new file, if needed)

## Acceptance Criteria

Given a POST request to import a recipe
When the RecipeImportResource processes the request
Then it should use application services instead of direct repository access

Given the updated RecipeImportResource
When checking dependencies
Then it should not have direct repository dependencies

Given the updated RecipeImportResource
When processing import requests
Then the REST API behavior should remain unchanged

## Testing Requirements

- Unit tests for import endpoint with mocked application services
- Integration tests to verify import functionality remains unchanged
- Tests for exception handling and HTTP response mapping

## Dependencies / Preconditions

- Recipe domain entity must exist
- Repository implementations must exist
- RecipeExtractionService interface must exist