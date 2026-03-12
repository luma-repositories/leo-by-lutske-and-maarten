# Create domain RecipeTitle value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a RecipeTitle value object in the domain layer to represent recipe titles with proper validation and encapsulation, replacing primitive String usage.

## Scope

- Create RecipeTitle value object as a Java record
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

Create a RecipeTitle value object that:
- Uses Java record for immutability
- Accepts String value in constructor
- Validates input is not null and not empty
- Validates title length does not exceed 255 characters
- Provides value() method to access the underlying String
- Throws IllegalArgumentException for invalid input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/RecipeTitle.java (new file)

## Acceptance Criteria

Given a valid recipe title string
When creating a RecipeTitle value object
Then the RecipeTitle should be created successfully and store the value

Given a null recipe title
When creating a RecipeTitle value object
Then an IllegalArgumentException should be thrown

Given an empty recipe title string
When creating a RecipeTitle value object
Then an IllegalArgumentException should be thrown

Given a recipe title longer than 255 characters
When creating a RecipeTitle value object
Then an IllegalArgumentException should be thrown

Given a RecipeTitle value object
When accessing the value
Then the original string should be returned

## Testing Requirements

- Unit tests for valid RecipeTitle creation
- Unit tests for null input validation
- Unit tests for empty string input validation
- Unit tests for maximum length validation
- Unit tests for value() method access

## Dependencies / Preconditions

None