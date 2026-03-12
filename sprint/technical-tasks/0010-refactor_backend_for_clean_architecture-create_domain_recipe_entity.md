# Create domain Recipe entity

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a Recipe domain entity in the domain layer that encapsulates recipe business logic and behavior, replacing the current JPA-based RecipeEntity.

## Scope

- Create Recipe domain entity as a Java class
- Use domain value objects for all properties
- Include business behavior methods
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- JPA annotations or persistence concerns (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)
- Database mapping (handled in infrastructure layer tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0001-refactor_backend_for_clean_architecture-create_domain_recipe_id_value_object.md
- 0002-refactor_backend_for_clean_architecture-create_domain_recipe_title_value_object.md
- 0004-refactor_backend_for_clean_architecture-create_domain_ingredients_value_object.md
- 0005-refactor_backend_for_clean_architecture-create_domain_preparation_steps_value_object.md
- 0008-refactor_backend_for_clean_architecture-create_domain_view_count_value_object.md
- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md

## Implementation Details

Create a Recipe domain entity that:
- Uses RecipeId, RecipeTitle, Ingredients, PreparationSteps, ViewCount, and Category
- Provides constructor for creating new recipes
- Provides constructor for reconstituting existing recipes
- Includes incrementViewCount() method that returns new Recipe with incremented count
- Includes equals() and hashCode() based on RecipeId
- Provides getter methods for all properties
- Contains no framework dependencies (pure Java)
- Implements proper encapsulation of business rules

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/entity/Recipe.java (new file)

## Acceptance Criteria

Given valid recipe properties
When creating a Recipe entity
Then the Recipe should be created successfully with all properties

Given a Recipe entity
When accessing any property
Then the correct value object should be returned

Given a Recipe entity
When calling incrementViewCount
Then a new Recipe with incremented view count should be returned

Given two Recipe entities with the same id
When comparing for equality
Then they should be equal

Given two Recipe entities with different ids
When comparing for equality
Then they should not be equal

## Testing Requirements

- Unit tests for Recipe creation with valid inputs
- Unit tests for property access methods
- Unit tests for incrementViewCount() behavior
- Unit tests for equals() and hashCode() behavior
- Unit tests for constructor validation

## Dependencies / Preconditions

- All required value objects must exist
- Category entity must exist