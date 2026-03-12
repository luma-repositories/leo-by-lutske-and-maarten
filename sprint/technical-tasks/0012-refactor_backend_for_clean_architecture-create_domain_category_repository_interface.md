# Create domain CategoryRepository interface

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a CategoryRepository interface in the domain layer that defines the contract for category persistence operations, following clean architecture dependency inversion principle.

## Scope

- Create CategoryRepository interface in domain layer
- Define methods for common category operations
- Use domain entities and value objects in method signatures
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Implementation of the repository (handled in infrastructure layer)
- JPA or database concerns (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md
- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md

## Implementation Details

Create a CategoryRepository interface that:
- Defines findById(CategoryId id) method returning Optional<Category>
- Defines findAll() method returning List<Category>
- Defines save(Category category) method returning void
- Defines delete(CategoryId id) method returning void
- Uses domain entities and value objects in all method signatures
- Contains no framework dependencies (pure Java)
- Follows repository pattern conventions

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/repository/CategoryRepository.java (new file)

## Acceptance Criteria

Given the CategoryRepository interface
When reviewing the method signatures
Then all methods should use domain entities and value objects

Given the CategoryRepository interface
When checking for framework dependencies
Then no framework imports should be present

Given the CategoryRepository interface
When examining the method definitions
Then all common category operations should be covered

## Testing Requirements

- Interface compilation tests
- Documentation tests for method contracts

## Dependencies / Preconditions

- CategoryId value object must exist
- Category entity must exist