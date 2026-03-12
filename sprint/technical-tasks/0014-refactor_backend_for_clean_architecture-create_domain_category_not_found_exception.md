# Create domain CategoryNotFoundException

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a CategoryNotFoundException in the domain layer to represent the business exception when a category cannot be found, following clean architecture principles.

## Scope

- Create CategoryNotFoundException as a domain exception
- Include CategoryId in exception details
- Provide meaningful error messages
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- HTTP status code mapping (handled in interface adapter layer)
- Framework-specific exception handling (handled in infrastructure layer)
- REST API error responses (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md

## Implementation Details

Create a CategoryNotFoundException that:
- Extends RuntimeException
- Accepts CategoryId in constructor
- Provides meaningful error message including the category ID
- Includes getter method for CategoryId
- Contains no framework dependencies (pure Java)
- Follows standard exception conventions

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/exception/CategoryNotFoundException.java (new file)

## Acceptance Criteria

Given a CategoryId
When creating a CategoryNotFoundException
Then the exception should be created with a meaningful message including the category ID

Given a CategoryNotFoundException
When accessing the CategoryId
Then the original CategoryId should be returned

Given a CategoryNotFoundException
When checking the exception message
Then it should contain the category ID value

## Testing Requirements

- Unit tests for exception creation with CategoryId
- Unit tests for message content validation
- Unit tests for CategoryId accessor method

## Dependencies / Preconditions

- CategoryId value object must exist