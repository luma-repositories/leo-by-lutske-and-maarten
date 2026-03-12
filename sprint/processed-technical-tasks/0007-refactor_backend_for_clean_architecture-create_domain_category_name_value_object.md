# Create domain CategoryName value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a CategoryName value object in the domain layer to represent category names with proper validation and encapsulation, replacing primitive String usage.

## Scope

- Create CategoryName value object as a Java record
- Add validation for non-null, non-empty, and maximum length
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Integration with existing code (handled in later tasks)
- Database mapping (handled in infrastructure layer tasks)
- REST API integration (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

None

## Implementation Details

Create a CategoryName value object that:
- Uses Java record for immutability
- Accepts String value in constructor
- Validates input is not null and not empty
- Validates name length does not exceed 100 characters
- Provides value() method to access the underlying String
- Throws IllegalArgumentException for invalid input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/CategoryName.java (new file)

## Acceptance Criteria

Given a valid category name string
When creating a CategoryName value object
Then the CategoryName should be created successfully and store the value

Given a null category name
When creating a CategoryName value object
Then an IllegalArgumentException should be thrown

Given an empty category name string
When creating a CategoryName value object
Then an IllegalArgumentException should be thrown

Given a category name longer than 100 characters
When creating a CategoryName value object
Then an IllegalArgumentException should be thrown

Given a CategoryName value object
When accessing the value
Then the original string should be returned

## Testing Requirements

- Unit tests for valid CategoryName creation
- Unit tests for null input validation
- Unit tests for empty string input validation
- Unit tests for maximum length validation
- Unit tests for value() method access

## Dependencies / Preconditions

None