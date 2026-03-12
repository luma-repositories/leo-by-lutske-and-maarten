# Create ListCategoriesUseCase application service

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a ListCategoriesUseCase application service that orchestrates the retrieval of all categories, following clean architecture principles and encapsulating the business workflow.

## Scope

- Create ListCategoriesUseCase application service
- Implement category listing workflow
- Use domain repository interface
- Place in application layer following clean architecture principles

## Out of Scope

- Repository implementation (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database concerns (handled in infrastructure layer)

## Clean Architecture Placement

- usecases

## Execution Dependencies

- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md
- 0012-refactor_backend_for_clean_architecture-create_domain_category_repository_interface.md

## Implementation Details

Create a ListCategoriesUseCase that:
- Accepts CategoryRepository as constructor dependency
- Provides execute() method returning List<Category>
- Uses repository.findAll() to retrieve all categories
- Returns empty list when no categories exist
- Contains no framework dependencies (pure Java)
- Follows single responsibility principle

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/application/usecase/ListCategoriesUseCase.java (new file)

## Acceptance Criteria

Given categories exist in the repository
When executing ListCategoriesUseCase
Then all categories should be returned as a list

Given no categories exist in the repository
When executing ListCategoriesUseCase
Then an empty list should be returned

Given a ListCategoriesUseCase
When checking dependencies
Then it should only depend on domain interfaces

## Testing Requirements

- Unit tests for successful category listing with multiple categories
- Unit tests for empty category list scenario
- Unit tests with mocked repository
- Unit tests for constructor dependency injection

## Dependencies / Preconditions

- Category entity must exist
- CategoryRepository interface must exist