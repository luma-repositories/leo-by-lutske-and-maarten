# Create integration tests for repository implementations

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create integration tests for JPA repository implementations to verify proper mapping between domain entities and JPA entities, and correct database operations.

## Scope

- Create integration tests for JpaRecipeRepository
- Create integration tests for JpaCategoryRepository
- Test domain-to-JPA entity mapping
- Test database operations with test database
- Verify data persistence and retrieval
- Use Quarkus test framework

## Out of Scope

- Unit testing (handled in other tasks)
- Application service integration testing (handled in other tasks)
- REST API integration testing (handled in other tasks)

## Clean Architecture Placement

- testing

## Execution Dependencies

- 0020-refactor_backend_for_clean_architecture-create_jpa_recipe_repository_implementation.md
- 0021-refactor_backend_for_clean_architecture-create_jpa_category_repository_implementation.md

## Implementation Details

Create integration tests that:
- Use @QuarkusTest annotation for Quarkus test context
- Use test database (H2 or TestContainers)
- Test all repository methods with real database operations
- Verify domain entity to JPA entity mapping
- Test ingredient string parsing and formatting
- Test transaction handling
- Clean up test data between tests
- Use @Transactional for test isolation

## Files / Modules Impacted

- backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/JpaRecipeRepositoryIT.java (new file)
- backend/src/test/java/be/lutske/leolegacy/infrastructure/persistence/JpaCategoryRepositoryIT.java (new file)

## Acceptance Criteria

Given a JpaRecipeRepository
When saving and retrieving Recipe domain entities
Then the mapping to/from JPA entities should work correctly

Given a JpaCategoryRepository
When saving and retrieving Category domain entities
Then the mapping to/from JPA entities should work correctly

Given repository operations
When performing database operations
Then data should be correctly persisted and retrieved

Given ingredient collections
When saving and retrieving recipes
Then ingredient parsing/formatting should work correctly

## Testing Requirements

- Integration tests for all repository methods
- Tests for domain-to-JPA entity mapping
- Tests for database persistence and retrieval
- Tests for ingredient string handling
- Transaction and cleanup verification

## Dependencies / Preconditions

- JPA repository implementations must exist
- Domain entities and value objects must exist
- Test database configuration must be available
- Quarkus test framework must be configured