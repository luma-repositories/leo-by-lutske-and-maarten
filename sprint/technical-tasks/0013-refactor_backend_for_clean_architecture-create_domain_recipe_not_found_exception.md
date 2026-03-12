# Create domain RecipeNotFoundException

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a RecipeNotFoundException in the domain layer to represent the business exception when a recipe cannot be found, following clean architecture principles.

## Scope

- Create RecipeNotFoundException as a domain exception
- Include RecipeId in exception details
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

- 0001-refactor_backend_for_clean_architecture-create_domain_recipe_id_value_object.md

## Implementation Details

Create a RecipeNotFoundException that:
- Extends RuntimeException
- Accepts RecipeId in constructor
- Provides meaningful error message including the recipe ID
- Includes getter method for RecipeId
- Contains no framework dependencies (pure Java)
- Follows standard exception conventions

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/exception/RecipeNotFoundException.java (new file)

## Acceptance Criteria

Given a RecipeId
When creating a RecipeNotFoundException
Then the exception should be created with a meaningful message including the recipe ID

Given a RecipeNotFoundException
When accessing the RecipeId
Then the original RecipeId should be returned

Given a RecipeNotFoundException
When checking the exception message
Then it should contain the recipe ID value

## Testing Requirements

- Unit tests for exception creation with RecipeId
- Unit tests for message content validation
- Unit tests for RecipeId accessor method

## Dependencies / Preconditions

- RecipeId value object must exist