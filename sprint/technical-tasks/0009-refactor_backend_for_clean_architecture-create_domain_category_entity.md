# Create domain Category entity

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a Category domain entity in the domain layer that encapsulates category business logic and behavior, replacing the current JPA-based CategoryEntity.

## Scope

- Create Category domain entity as a Java class
- Use domain value objects for all properties
- Include business behavior methods
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- JPA annotations or persistence concerns (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database mapping (handled in infrastructure layer tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md
- 0007-refactor_backend_for_clean_architecture-create_domain_category_name_value_object.md

## Implementation Details

Create a Category domain entity that:
- Uses CategoryId and CategoryName value objects
- Provides constructor for creating new categories
- Provides constructor for reconstituting existing categories
- Includes equals() and hashCode() based on CategoryId
- Provides getter methods for all properties
- Contains no framework dependencies (pure Java)
- Implements proper encapsulation of business rules

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/entity/Category.java (new file)

## Acceptance Criteria

Given valid category id and name
When creating a Category entity
Then the Category should be created successfully with all properties

Given a Category entity
When accessing the id
Then the CategoryId should be returned

Given a Category entity
When accessing the name
Then the CategoryName should be returned

Given two Category entities with the same id
When comparing for equality
Then they should be equal

Given two Category entities with different ids
When comparing for equality
Then they should not be equal

## Testing Requirements

- Unit tests for Category creation with valid inputs
- Unit tests for property access methods
- Unit tests for equals() and hashCode() behavior
- Unit tests for constructor validation

## Dependencies / Preconditions

- CategoryId value object must exist
- CategoryName value object must exist