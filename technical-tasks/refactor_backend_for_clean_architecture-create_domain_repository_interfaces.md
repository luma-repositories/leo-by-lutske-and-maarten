# Create domain repository interfaces

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Define domain repository interfaces that abstract data access concerns from the domain layer, enabling dependency inversion and testability.

## Scope

- Create RecipeRepository interface with domain-focused operations
- Create CategoryRepository interface with domain-focused operations
- Define repository methods using domain entities and value objects
- Establish clear contracts for data persistence and retrieval
- Remove direct dependency on Panache repositories from domain layer

## Out of Scope

- Implementation of repository interfaces (handled in infrastructure adapter task)
- Modification of existing Panache repositories (separate task)
- Use case implementation (separate task)
- JPA entity changes (separate task)

## Implementation Details

### RecipeRepository Interface

Create `be.lutske.leolegacy.domain.repository.RecipeRepository` with:

**Query Methods:**
- `Optional<Recipe> findById(RecipeId id)` - find recipe by domain ID
- `List<Recipe> findAll()` - retrieve all recipes
- `List<Recipe> findByCategory(CategoryId categoryId)` - find recipes in category
- `List<Recipe> findByTitleContaining(String titleFragment)` - search by title
- `boolean existsById(RecipeId id)` - check if recipe exists
- `long count()` - count total recipes
- `long countByCategory(CategoryId categoryId)` - count recipes in category

**Persistence Methods:**
- `Recipe save(Recipe recipe)` - save new or update existing recipe
- `void deleteById(RecipeId id)` - delete recipe by ID
- `void delete(Recipe recipe)` - delete recipe entity

**Business Query Methods:**
- `List<Recipe> findRecentlyCreated(int limit)` - find recently created recipes
- `List<Recipe> findRecentlyUpdated(int limit)` - find recently updated recipes
- `Optional<Recipe> findByTitleExact(String title)` - find recipe by exact title match

### CategoryRepository Interface

Create `be.lutske.leolegacy.domain.repository.CategoryRepository` with:

**Query Methods:**
- `Optional<Category> findById(CategoryId id)` - find category by domain ID
- `List<Category> findAll()` - retrieve all categories
- `Optional<Category> findByName(String name)` - find category by name
- `boolean existsById(CategoryId id)` - check if category exists
- `boolean existsByName(String name)` - check if category name exists
- `long count()` - count total categories

**Persistence Methods:**
- `Category save(Category category)` - save new or update existing category
- `void deleteById(CategoryId id)` - delete category by ID
- `void delete(Category category)` - delete category entity

**Business Query Methods:**
- `List<Category> findCategoriesWithRecipes()` - find categories that have recipes
- `List<Category> findEmptyCategories()` - find categories without recipes

### Repository Contracts

**Transaction Behavior:**
- All repository methods should participate in existing transactions
- Repository implementations should not manage transactions directly
- Save operations should return the persisted entity with updated timestamps

**Error Handling:**
- Repository methods should throw domain-specific exceptions
- Infrastructure exceptions should be translated to domain exceptions
- Null parameters should result in IllegalArgumentException

**Performance Considerations:**
- Query methods should support efficient database operations
- Bulk operations should be optimized for large datasets
- Lazy loading behavior should be clearly documented

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/repository/RecipeRepository.java`
- `be/lutske/leolegacy/domain/repository/CategoryRepository.java`

**Directory structure:**
```
be/lutske/leolegacy/
├── domain/
│   ├── model/
│   │   └── (existing domain entities)
│   ├── repository/
│   │   ├── RecipeRepository.java
│   │   └── CategoryRepository.java
│   └── exception/
│       └── (existing domain exceptions)
```

## Acceptance Criteria

**Given** a RecipeRepository interface is defined  
**When** it is reviewed for domain dependencies  
**Then** it should only reference domain entities and value objects

**Given** a RecipeRepository interface  
**When** query methods are examined  
**Then** they should use RecipeId and CategoryId value objects as parameters

**Given** a RecipeRepository interface  
**When** persistence methods are examined  
**Then** they should accept and return Recipe domain entities

**Given** a CategoryRepository interface is defined  
**When** it is reviewed for infrastructure dependencies  
**Then** it should have no JPA, Panache, or database-specific references

**Given** repository interfaces are defined  
**When** they are used in domain services  
**Then** they should enable dependency inversion through interface injection

**Given** repository methods are defined  
**When** their contracts are reviewed  
**Then** they should clearly specify expected behavior and exceptions

**Given** repository interfaces are created  
**When** they are examined for business relevance  
**Then** they should provide methods that support domain use cases

## Testing Requirements

**Interface Contract Tests:**

Create `RecipeRepositoryContract.java`:
- Define abstract test class for repository contract verification
- Test all query methods with various scenarios
- Test persistence methods with valid and invalid data
- Test error conditions and exception handling
- Verify transaction behavior expectations

Create `CategoryRepositoryContract.java`:
- Define abstract test class for repository contract verification
- Test all query methods with various scenarios
- Test persistence methods with valid and invalid data
- Test uniqueness constraints and business rules
- Verify error handling and exception translation

**Mock Implementation Tests:**

Create `MockRecipeRepositoryTest.java`:
- Create in-memory mock implementation for testing
- Verify interface contract compliance
- Test repository behavior in isolation
- Validate domain entity handling

Create `MockCategoryRepositoryTest.java`:
- Create in-memory mock implementation for testing
- Verify interface contract compliance
- Test repository behavior in isolation
- Validate domain entity handling

**Test Coverage Requirements:**
- All repository methods must have contract tests
- Error conditions must be tested and documented
- Mock implementations must demonstrate interface usability
- Repository interfaces must be testable without infrastructure dependencies