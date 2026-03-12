# Create unit tests for domain layer

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create comprehensive unit tests for all domain layer components (value objects, entities, exceptions) to ensure domain logic is properly tested in isolation.

## Scope

- Create unit tests for all value objects (RecipeId, RecipeTitle, Ingredient, Ingredients, etc.)
- Create unit tests for domain entities (Recipe, Category)
- Create unit tests for domain exceptions
- Ensure 100% coverage of domain business logic
- Use pure Java testing (no framework dependencies)

## Out of Scope

- Integration testing (handled in other tasks)
- Application service testing (handled in other tasks)
- Infrastructure testing (handled in other tasks)

## Clean Architecture Placement

- testing

## Execution Dependencies

- 0001-refactor_backend_for_clean_architecture-create_domain_recipe_id_value_object.md
- 0002-refactor_backend_for_clean_architecture-create_domain_recipe_title_value_object.md
- 0003-refactor_backend_for_clean_architecture-create_domain_ingredient_value_object.md
- 0004-refactor_backend_for_clean_architecture-create_domain_ingredients_value_object.md
- 0005-refactor_backend_for_clean_architecture-create_domain_preparation_steps_value_object.md
- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md
- 0007-refactor_backend_for_clean_architecture-create_domain_category_name_value_object.md
- 0008-refactor_backend_for_clean_architecture-create_domain_view_count_value_object.md
- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md
- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0013-refactor_backend_for_clean_architecture-create_domain_recipe_not_found_exception.md
- 0014-refactor_backend_for_clean_architecture-create_domain_category_not_found_exception.md

## Implementation Details

Create unit tests that:
- Test all value object validation rules
- Test all value object behavior methods
- Test domain entity creation and behavior
- Test domain entity equality and hash code
- Test domain exception creation and properties
- Use JUnit 5 and AssertJ for assertions
- Follow AAA pattern (Arrange, Act, Assert)
- Include edge cases and boundary conditions
- Test immutability of value objects and entities

## Files / Modules Impacted

- backend/src/test/java/be/lutske/leolegacy/domain/valueobject/ (multiple test files)
- backend/src/test/java/be/lutske/leolegacy/domain/entity/ (multiple test files)
- backend/src/test/java/be/lutske/leolegacy/domain/exception/ (multiple test files)

## Acceptance Criteria

Given all domain value objects
When running their unit tests
Then all validation rules and behavior should be verified

Given all domain entities
When running their unit tests
Then all business logic and behavior should be verified

Given all domain exceptions
When running their unit tests
Then exception creation and properties should be verified

Given the domain test suite
When checking code coverage
Then it should achieve 100% coverage of domain logic

## Testing Requirements

- Unit tests for all value objects with validation scenarios
- Unit tests for all entities with business logic scenarios
- Unit tests for all exceptions
- Code coverage verification
- Fast execution (no external dependencies)

## Dependencies / Preconditions

- All domain layer components must exist
- JUnit 5 and testing dependencies must be available