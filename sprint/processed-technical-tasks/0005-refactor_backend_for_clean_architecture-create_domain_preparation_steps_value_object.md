# Create domain PreparationSteps value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a PreparationSteps value object in the domain layer to represent recipe preparation instructions with proper encapsulation and validation.

## Scope

- Create PreparationSteps value object as a Java record
- Encapsulate preparation instructions as structured data
- Add validation for required content
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Complex step parsing or formatting (handled in infrastructure layer)
- Database mapping (handled in infrastructure layer tasks)
- REST API integration (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

None

## Implementation Details

Create a PreparationSteps value object that:
- Uses Java record for immutability
- Contains String value for preparation instructions
- Validates content is not null and not empty
- Provides value() method to access the instructions
- Throws IllegalArgumentException for invalid input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/PreparationSteps.java (new file)

## Acceptance Criteria

Given valid preparation instructions
When creating a PreparationSteps value object
Then the PreparationSteps should be created successfully and store the instructions

Given null preparation instructions
When creating a PreparationSteps value object
Then an IllegalArgumentException should be thrown

Given empty preparation instructions
When creating a PreparationSteps value object
Then an IllegalArgumentException should be thrown

Given a PreparationSteps value object
When accessing the value
Then the original instructions should be returned

## Testing Requirements

- Unit tests for valid PreparationSteps creation
- Unit tests for null input validation
- Unit tests for empty string input validation
- Unit tests for value() method access

## Dependencies / Preconditions

None