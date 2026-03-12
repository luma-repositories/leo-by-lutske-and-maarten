# Create unit tests for application services

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create comprehensive unit tests for all application services (use cases) to ensure business workflows are properly tested with mocked dependencies.

## Scope

- Create unit tests for all use cases (GetRecipeUseCase, ListRecipesUseCase, etc.)
- Mock domain repository interfaces
- Test successful scenarios and error scenarios
- Verify proper interaction with repositories
- Use pure Java testing with mocking framework

## Out of Scope

- Integration testing with real repositories (handled in other tasks)
- Domain layer testing (handled in other tasks)
- Infrastructure testing (handled in other tasks)

## Clean Architecture Placement

- testing

## Execution Dependencies

- 0015-refactor_backend_for_clean_architecture-create_get_recipe_use_case.md
- 0016-refactor_backend_for_clean_architecture-create_list_recipes_use_case.md
- 0017-refactor_backend_for_clean_architecture-create_list_recipes_by_category_use_case.md
- 0018-refactor_backend_for_clean_architecture-create_increment_recipe_view_count_use_case.md
- 0019-refactor_backend_for_clean_architecture-create_list_categories_use_case.md

## Implementation Details

Create unit tests that:
- Mock repository interfaces using Mockito
- Test successful execution paths for all use cases
- Test error scenarios (entity not found, etc.)
- Verify repository method calls and parameters
- Test business logic within use cases
- Use JUnit 5 and Mockito for mocking
- Follow AAA pattern (Arrange, Act, Assert)
- Include edge cases and boundary conditions

## Files / Modules Impacted

- backend/src/test/java/be/lutske/leolegacy/application/usecase/ (multiple test files)

## Acceptance Criteria

Given all application use cases
When running their unit tests with mocked repositories
Then all business workflows should be verified

Given use cases with error scenarios
When running tests for exception cases
Then proper exception handling should be verified

Given use case tests
When checking repository interactions
Then all repository calls should be properly verified

Given the application test suite
When checking code coverage
Then it should achieve high coverage of application logic

## Testing Requirements

- Unit tests for all use cases with successful scenarios
- Unit tests for all use cases with error scenarios
- Mock verification for repository interactions
- Code coverage verification
- Fast execution with mocked dependencies

## Dependencies / Preconditions

- All application use cases must exist
- Domain repository interfaces must exist
- JUnit 5 and Mockito dependencies must be available