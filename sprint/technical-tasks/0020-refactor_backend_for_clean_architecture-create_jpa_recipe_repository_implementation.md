# Create JPA RecipeRepository implementation

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create a JPA-based implementation of the RecipeRepository interface in the infrastructure layer, providing persistence capabilities while maintaining clean architecture boundaries.

## Scope

- Create JpaRecipeRepository implementing RecipeRepository interface
- Map between domain Recipe entities and JPA RecipeEntity
- Use existing Panache repository for database operations
- Place in infrastructure layer following clean architecture principles
- Use Quarkus/JPA frameworks as needed

## Out of Scope

- Changes to domain layer (already implemented)
- REST API integration (handled in interface adapter tasks)
- Application service changes (already implemented)

## Clean Architecture Placement

- infrastructure

## Execution Dependencies

- 0010-refactor_backend_for_clean_architecture-create_domain_recipe_entity.md
- 0011-refactor_backend_for_clean_architecture-create_domain_recipe_repository_interface.md

## Implementation Details

Create a JpaRecipeRepository that:
- Implements RecipeRepository interface from domain layer
- Uses existing RecipeRepository (Panache) for database operations
- Creates mapper methods between Recipe domain entity and RecipeEntity JPA entity
- Handles ingredient string parsing/formatting for database storage
- Implements all repository methods (findById, findAll, findByCategory, save, delete)
- Uses @ApplicationScoped annotation for CDI
- Handles Optional conversions properly

## Files / Modules Impacted

- backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/JpaRecipeRepository.java (new file)

## Acceptance Criteria

Given a RecipeId
When calling findById on JpaRecipeRepository
Then it should delegate to Panache repository and map the result to domain Recipe

Given a Recipe domain entity
When calling save on JpaRecipeRepository
Then it should map to JPA entity and persist using Panache repository

Given a CategoryId
When calling findByCategory on JpaRecipeRepository
Then it should retrieve and map all recipes for that category

Given the JpaRecipeRepository
When checking dependencies
Then it should implement the domain repository interface

## Testing Requirements

- Unit tests for all repository methods
- Integration tests with test database
- Tests for domain-to-JPA entity mapping
- Tests for ingredient string parsing/formatting

## Dependencies / Preconditions

- Recipe domain entity must exist
- RecipeRepository interface must exist
- Existing JPA entities and Panache repositories must exist