# Refactor CategoryResource to use application services

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Refactor the existing CategoryResource REST controller to use application services instead of directly accessing infrastructure repositories, following clean architecture principles.

## Scope

- Update CategoryResource to inject application services instead of repositories
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

- 0019-refactor_backend_for_clean_architecture-create_list_categories_use_case.md

## Implementation Details

Update CategoryResource to:
- Inject ListCategoriesUseCase
- Remove direct CategoryRepository injection
- Replace repository.findAll() calls with listCategoriesUseCase.execute()
- Update mapping from domain entities to DTOs
- Handle domain exceptions and map to appropriate HTTP responses

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java (modify existing)

## Acceptance Criteria

Given a GET request to /categories
When the CategoryResource processes the request
Then it should use ListCategoriesUseCase

Given the updated CategoryResource
When checking dependencies
Then it should not have direct repository dependencies

Given the updated CategoryResource
When processing requests
Then the REST API behavior should remain unchanged

## Testing Requirements

- Unit tests for all endpoint methods with mocked use cases
- Integration tests to verify REST API behavior remains unchanged
- Tests for exception handling and HTTP response mapping

## Dependencies / Preconditions

- ListCategoriesUseCase must exist
- Domain entities and value objects must exist