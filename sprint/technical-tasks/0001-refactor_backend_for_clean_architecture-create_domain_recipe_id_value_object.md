# Create domain RecipeId value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a RecipeId value object in the domain layer to represent recipe identifiers with proper encapsulation and validation, replacing primitive String usage throughout the codebase.

## Scope

- Create RecipeId value object as a Java record
- Add basic validation for non-null and non-empty values
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

Create a RecipeId value object that:
- Uses Java record for immutability
- Accepts String value in constructor
- Validates input is not null and not empty
- Provides value() method to access the underlying String
- Throws IllegalArgumentException for invalid input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/RecipeId.java (new file)

## Acceptance Criteria

Given a valid recipe identifier string
When creating a RecipeId value object
Then the RecipeId should be created successfully and store the value

Given a null recipe identifier
When creating a RecipeId value object
Then an IllegalArgumentException should be thrown

Given an empty recipe identifier string
When creating a RecipeId value object
Then an IllegalArgumentException should be thrown

Given a RecipeId value object
When accessing the value
Then the original string should be returned

## Testing Requirements

- Unit tests for valid RecipeId creation
- Unit tests for null input validation
- Unit tests for empty string input validation
- Unit tests for value() method access

## Dependencies / Preconditions

None