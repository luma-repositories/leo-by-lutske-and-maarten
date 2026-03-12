# Create domain Ingredient value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create an Ingredient value object in the domain layer to represent individual recipe ingredients with proper structure, replacing the current pipe-separated string approach.

## Scope

- Create Ingredient value object as a Java record
- Include name, amount, and unit fields
- Add validation for required fields
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Complex unit conversion logic (can be added later)
- Integration with existing code (handled in later tasks)
- Database mapping (handled in infrastructure layer tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

None

## Implementation Details

Create an Ingredient value object that:
- Uses Java record for immutability
- Contains name (String), amount (String), and unit (String) fields
- Validates name is not null and not empty
- Allows amount and unit to be null or empty (optional fields)
- Provides accessor methods for all fields
- Throws IllegalArgumentException for invalid name
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/Ingredient.java (new file)

## Acceptance Criteria

Given a valid ingredient name with amount and unit
When creating an Ingredient value object
Then the Ingredient should be created successfully with all values

Given a valid ingredient name without amount or unit
When creating an Ingredient value object
Then the Ingredient should be created successfully with name only

Given a null ingredient name
When creating an Ingredient value object
Then an IllegalArgumentException should be thrown

Given an empty ingredient name
When creating an Ingredient value object
Then an IllegalArgumentException should be thrown

Given an Ingredient value object
When accessing the fields
Then the original values should be returned

## Testing Requirements

- Unit tests for valid Ingredient creation with all fields
- Unit tests for valid Ingredient creation with name only
- Unit tests for null name validation
- Unit tests for empty name validation
- Unit tests for field accessor methods

## Dependencies / Preconditions

None