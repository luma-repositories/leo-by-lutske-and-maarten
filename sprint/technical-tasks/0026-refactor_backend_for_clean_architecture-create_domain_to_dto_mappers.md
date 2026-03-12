# Create domain to DTO mappers

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create mapper classes to convert between domain entities and REST DTOs, maintaining clean separation between domain and interface adapter layers.

## Scope

- Create RecipeMapper for Recipe domain entity to DTO conversion
- Create CategoryMapper for Category domain entity to DTO conversion
- Handle value object to primitive conversions
- Place mappers in interface adapter layer
- Use pure Java mapping logic

## Out of Scope

- Complex mapping frameworks (keep it simple)
- Database entity mapping (handled in infrastructure layer)
- Validation logic (handled in domain layer)

## Clean Architecture Placement

- interface adapters

## Execution Dependencies

- 0009-refactor_backend_for_clean_architecture-create_domain_category_entity.md
- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md

## Implementation Details

Create mapper classes that:
- Convert Recipe domain entity to RecipeDetailResponse and RecipeSummaryResponse DTOs
- Convert Category domain entity to CategoryResponse DTO
- Handle value object to primitive type conversions
- Convert Ingredients to pipe-separated string for DTO compatibility
- Use static methods for stateless mapping
- Handle null safety appropriately
- Follow existing DTO structure contracts

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeMapper.java (new file)
- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/mapper/CategoryMapper.java (new file)

## Acceptance Criteria

Given a Recipe domain entity
When mapping to RecipeDetailResponse
Then all domain properties should be correctly converted to DTO fields

Given a Recipe domain entity
When mapping to RecipeSummaryResponse
Then summary fields should be correctly converted to DTO fields

Given a Category domain entity
When mapping to CategoryResponse
Then all domain properties should be correctly converted to DTO fields

Given domain entities with value objects
When mapping to DTOs
Then value objects should be converted to appropriate primitive types

## Testing Requirements

- Unit tests for Recipe to RecipeDetailResponse mapping
- Unit tests for Recipe to RecipeSummaryResponse mapping
- Unit tests for Category to CategoryResponse mapping
- Unit tests for null handling
- Unit tests for ingredient list to string conversion

## Dependencies / Preconditions

- Recipe domain entity must exist
- Category domain entity must exist
- Existing DTO classes must exist