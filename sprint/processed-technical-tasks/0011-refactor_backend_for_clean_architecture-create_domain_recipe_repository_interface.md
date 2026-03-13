# Create domain RecipeRepository interface

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a RecipeRepository interface in the domain layer that defines the contract for recipe persistence operations, following clean architecture dependency inversion principle.

## Scope

- Create RecipeRepository interface in domain layer
- Define methods for common recipe operations
- Use domain entities and value objects in method signatures
- Place in domain layer following clean architecture principles
- Use pure Java only (no framework dependencies)

## Out of Scope

- Implementation of the repository (handled in infrastructure layer)
- JPA or database concerns (handled in infrastructure layer)
- REST API integration (handled in interface adapter tasks)

## Clean Architecture Placement

- domain

## Execution Dependencies

- 0001-refactor_backend_for_clean_architecture-create_domain_recipe_id_value_object.md
- 0006-refactor_backend_for_clean_architecture-create_domain_category_id_value_object.md
- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md

## Implementation Details

Create a RecipeRepository interface that:
- Defines findById(RecipeId id) method returning Optional<Recipe>
- Defines findAll() method returning List<Recipe>
- Defines findByCategory(CategoryId categoryId) method returning List<Recipe>
- Defines save(Recipe recipe) method returning void
- Defines delete(RecipeId id) method returning void
- Uses domain entities and value objects in all method signatures
- Contains no framework dependencies (pure Java)
- Follows repository pattern conventions

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/domain/repository/RecipeRepository.java (new file)

## Acceptance Criteria

Given the RecipeRepository interface
When reviewing the method signatures
Then all methods should use domain entities and value objects

Given the RecipeRepository interface
When checking for framework dependencies
Then no framework imports should be present

Given the RecipeRepository interface
When examining the method definitions
Then all common recipe operations should be covered

## Testing Requirements

- Interface compilation tests
- Documentation tests for method contracts

## Dependencies / Preconditions

- RecipeId value object must exist
- CategoryId value object must exist
- Recipe entity must exist