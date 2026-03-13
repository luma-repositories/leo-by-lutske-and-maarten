# Create JPA CategoryRepository implementation

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a JPA-based implementation of the CategoryRepository interface in the infrastructure layer, providing persistence capabilities while maintaining clean architecture boundaries.

## Scope

- Create JpaCategoryRepository implementing CategoryRepository interface
- Map between domain Category entities and JPA CategoryEntity
- Use existing Panache repository for database operations
- Place in infrastructure layer following clean architecture principles
- Use Quarkus/JPA frameworks as needed

## Out of Scope

- Changes to domain layer (already implemented)
- REST API integration (handled in interface adapter tasks)
- Application service changes (already implemented)

## Clean Architecture Placement

- infrastructure

## Execution Dependencies

- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md
- 0012-refactor_backend_for_clean_architecture-create_domain_category_repository_interface.md

## Implementation Details

Create a JpaCategoryRepository that:
- Implements CategoryRepository interface from domain layer
- Uses existing CategoryRepository (Panache) for database operations
- Creates mapper methods between Category domain entity and CategoryEntity JPA entity
- Implements all repository methods (findById, findAll, save, delete)
- Uses @ApplicationScoped annotation for CDI
- Handles Optional conversions properly

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/JpaCategoryRepository.java (new file)

## Acceptance Criteria

Given a CategoryId
When calling findById on JpaCategoryRepository
Then it should delegate to Panache repository and map the result to domain Category

Given a Category domain entity
When calling save on JpaCategoryRepository
Then it should map to JPA entity and persist using Panache repository

Given the JpaCategoryRepository
When checking dependencies
Then it should implement the domain repository interface

## Testing Requirements

- Unit tests for all repository methods
- Integration tests with test database
- Tests for domain-to-JPA entity mapping

## Dependencies / Preconditions

- Category domain entity must exist
- CategoryRepository interface must exist
- Existing JPA entities and Panache repositories must exist