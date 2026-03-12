# Create domain ViewCount value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a ViewCount value object in the domain layer to represent recipe view counts with proper encapsulation and validation, replacing primitive int usage.

## Scope

- Create ViewCount value object as a Java record
- Add validation for non-negative values
- Include increment behavior
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

Create a ViewCount value object that:
- Uses Java record for immutability
- Accepts int value in constructor
- Validates input is not negative (>= 0)
- Provides value() method to access the underlying int
- Provides increment() method that returns new ViewCount with value + 1
- Throws IllegalArgumentException for negative input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/ViewCount.java (new file)

## Acceptance Criteria

Given a valid non-negative view count
When creating a ViewCount value object
Then the ViewCount should be created successfully and store the value

Given a negative view count
When creating a ViewCount value object
Then an IllegalArgumentException should be thrown

Given a ViewCount value object
When accessing the value
Then the original int should be returned

Given a ViewCount value object
When calling increment
Then a new ViewCount with value + 1 should be returned

## Testing Requirements

- Unit tests for valid ViewCount creation with zero
- Unit tests for valid ViewCount creation with positive value
- Unit tests for negative input validation
- Unit tests for value() method access
- Unit tests for increment() method behavior

## Dependencies / Preconditions

None