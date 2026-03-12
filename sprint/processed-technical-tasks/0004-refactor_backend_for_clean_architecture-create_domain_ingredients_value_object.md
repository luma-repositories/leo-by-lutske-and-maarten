# Create domain Ingredients value object

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create an Ingredients value object in the domain layer to represent a collection of recipe ingredients with proper encapsulation and business rules, replacing the current pipe-separated string storage.

## Scope

- Create Ingredients value object as a Java record
- Encapsulate List<Ingredient> with validation
- Add business rules for ingredient collections
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Ingredient parsing from strings (handled in infrastructure layer)
- Database mapping (handled in infrastructure layer tasks)
- REST API integration (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0003-refactor_backend_for_clean_architecture-create_domain_ingredient_value_object.md

## Implementation Details

Create an Ingredients value object that:
- Uses Java record for immutability
- Contains List<Ingredient> as internal collection
- Validates the list is not null
- Creates defensive copy of input list
- Provides size() method to get ingredient count
- Provides isEmpty() method to check if empty
- Provides iterator or stream access to ingredients
- Throws IllegalArgumentException for null input
- Contains no framework dependencies (pure Java)

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/Ingredients.java (new file)

## Acceptance Criteria

Given a valid list of ingredients
When creating an Ingredients value object
Then the Ingredients should be created successfully with all ingredients

Given an empty list of ingredients
When creating an Ingredients value object
Then the Ingredients should be created successfully as empty

Given a null list of ingredients
When creating an Ingredients value object
Then an IllegalArgumentException should be thrown

Given an Ingredients value object
When accessing the size
Then the correct number of ingredients should be returned

Given an Ingredients value object
When checking if empty
Then the correct boolean result should be returned

Given an Ingredients value object
When iterating over ingredients
Then all ingredients should be accessible

## Testing Requirements

- Unit tests for valid Ingredients creation with multiple ingredients
- Unit tests for valid Ingredients creation with empty list
- Unit tests for null list validation
- Unit tests for size() method
- Unit tests for isEmpty() method
- Unit tests for iteration access

## Dependencies / Preconditions

- Ingredient value object must exist