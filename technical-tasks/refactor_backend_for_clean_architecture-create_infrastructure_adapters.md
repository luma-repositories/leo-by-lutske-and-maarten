# Create infrastructure adapters for data access

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Implement infrastructure adapters that bridge domain repository interfaces with JPA/Panache persistence layer, maintaining clean separation between domain and infrastructure concerns.

## Scope

- Create JPA repository adapter implementations for domain repository interfaces
- Implement entity-to-domain mapping and domain-to-entity mapping
- Handle exception translation from infrastructure to domain exceptions
- Maintain existing JPA entities and Panache repositories
- Ensure proper dependency injection configuration
- Implement efficient query operations and data access patterns

## Out of Scope

- Domain repository interface changes (already defined)
- JPA entity modifications (maintain existing structure)
- Use case implementation changes (separate task)
- Database schema changes (maintain existing schema)

## Implementation Details

### Repository Adapter Implementation

Create `be.lutske.leolegacy.infrastructure.persistence.adapter.JpaRecipeRepositoryAdapter`:

**Dependencies:**
- `RecipeRepository recipeRepository` (Panache repository)
- `CategoryRepository categoryRepository` (Panache repository)
- `RecipeEntityMapper recipeEntityMapper`

**Interface Implementation:**
Implements `be.lutske.leolegacy.domain.repository.RecipeRepository`

**Method Implementations:**

`Optional<Recipe> findById(RecipeId id)`:
- Convert RecipeId to UUID
- Call `recipeRepository.findByIdOptional(uuid)`
- Map RecipeEntity to Recipe domain entity if found
- Return Optional<Recipe>

`List<Recipe> findAll()`:
- Call `recipeRepository.listAll()`
- Map List<RecipeEntity> to List<Recipe>
- Return domain entity list

`List<Recipe> findByCategory(CategoryId categoryId)`:
- Convert CategoryId to UUID
- Call `recipeRepository.find("category.id", categoryId.getValue())`
- Map results to domain entities
- Return domain entity list

`Recipe save(Recipe recipe)`:
- Map Recipe domain entity to RecipeEntity
- Handle new vs existing entity (check if ID exists)
- Call `recipeRepository.persist(entity)` or update existing
- Map saved entity back to domain entity
- Return saved Recipe

`void deleteById(RecipeId id)`:
- Convert RecipeId to UUID
- Verify entity exists (throw RecipeNotFoundException if not)
- Call `recipeRepository.deleteById(uuid)`

Create `be.lutske.leolegacy.infrastructure.persistence.adapter.JpaCategoryRepositoryAdapter`:

**Dependencies:**
- `CategoryRepository categoryRepository` (Panache repository)
- `CategoryEntityMapper categoryEntityMapper`

**Interface Implementation:**
Implements `be.lutske.leolegacy.domain.repository.CategoryRepository`

**Method Implementations:**

`Optional<Category> findById(CategoryId id)`:
- Convert CategoryId to UUID
- Call `categoryRepository.findByIdOptional(uuid)`
- Map CategoryEntity to Category domain entity if found
- Return Optional<Category>

`List<Category> findAll()`:
- Call `categoryRepository.listAll()`
- Map List<CategoryEntity> to List<Category>
- Return domain entity list

`Optional<Category> findByName(String name)`:
- Call `categoryRepository.find("name", name).firstResultOptional()`
- Map CategoryEntity to Category domain entity if found
- Return Optional<Category>

`Category save(Category category)`:
- Map Category domain entity to CategoryEntity
- Handle new vs existing entity
- Call `categoryRepository.persist(entity)` or update existing
- Map saved entity back to domain entity
- Return saved Category

### Entity Mapping Implementation

Create `be.lutske.leolegacy.infrastructure.persistence.mapper.RecipeEntityMapper`:

**Domain to Entity Mapping:**

`RecipeEntity toEntity(Recipe recipe)`:
- Map RecipeId to UUID
- Map Recipe properties to RecipeEntity properties
- Convert RecipeIngredient list to JSON or separate entity table
- Convert RecipeInstruction list to JSON or separate entity table
- Map CategoryId to CategoryEntity reference
- Handle timestamps (createdAt, updatedAt)

`Recipe toDomain(RecipeEntity entity)`:
- Map UUID to RecipeId
- Map RecipeEntity properties to Recipe properties
- Convert JSON or entity table to RecipeIngredient list
- Convert JSON or entity table to RecipeInstruction list
- Map CategoryEntity to CategoryId
- Handle timestamps

**Ingredient and Instruction Handling:**

Option 1 - JSON Storage (simpler):
- Store ingredients and instructions as JSON in existing text fields
- Use Jackson for JSON serialization/deserialization
- Maintain backward compatibility with existing data

Option 2 - Separate Entity Tables (normalized):
- Create separate entity tables for ingredients and instructions
- Maintain foreign key relationships
- Requires database migration

**Recommendation:** Use JSON storage for this refactoring to maintain existing schema.

Create `be.lutske.leolegacy.infrastructure.persistence.mapper.CategoryEntityMapper`:

**Domain to Entity Mapping:**

`CategoryEntity toEntity(Category category)`:
- Map CategoryId to UUID
- Map Category properties to CategoryEntity properties
- Handle timestamps

`Category toDomain(CategoryEntity entity)`:
- Map UUID to CategoryId
- Map CategoryEntity properties to Category properties
- Handle timestamps

### Exception Translation

Create `be.lutske.leolegacy.infrastructure.persistence.exception.PersistenceExceptionTranslator`:

**Exception Mapping:**

`PersistenceException` → `RepositoryException`
`EntityNotFoundException` → `RecipeNotFoundException` or `CategoryNotFoundException`
`ConstraintViolationException` → `InvalidRecipeDataException` or `InvalidCategoryDataException`
`DataIntegrityViolationException` → `DomainException`

**Translation Methods:**

`RuntimeException translateRecipeException(Exception ex, RecipeId id)`:
- Analyze exception type and message
- Create appropriate domain exception
- Preserve original exception as cause
- Include meaningful error messages

### Dependency Injection Configuration

**CDI Configuration:**

Ensure adapters are properly configured for injection:

```java
@ApplicationScoped
public class JpaRecipeRepositoryAdapter implements RecipeRepository {
    // implementation
}

@ApplicationScoped  
public class JpaCategoryRepositoryAdapter implements CategoryRepository {
    // implementation
}
```

**Producer Configuration (if needed):**

Create `be.lutske.leolegacy.infrastructure.persistence.config.RepositoryProducer`:
- Configure repository adapter beans
- Handle any complex dependency injection scenarios
- Ensure proper scoping and lifecycle management

### Data Access Optimization

**Query Optimization:**

- Use appropriate fetch strategies for related entities
- Implement efficient bulk operations where needed
- Add database indexes for frequently queried fields
- Use pagination for large result sets

**Caching Considerations:**

- Implement appropriate caching strategies for read-heavy operations
- Consider second-level cache for category data
- Ensure cache invalidation on entity updates

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/infrastructure/persistence/adapter/JpaRecipeRepositoryAdapter.java`
- `be/lutske/leolegacy/infrastructure/persistence/adapter/JpaCategoryRepositoryAdapter.java`
- `be/lutske/leolegacy/infrastructure/persistence/mapper/RecipeEntityMapper.java`
- `be/lutske/leolegacy/infrastructure/persistence/mapper/CategoryEntityMapper.java`
- `be/lutske/leolegacy/infrastructure/persistence/exception/PersistenceExceptionTranslator.java`
- `be/lutske/leolegacy/infrastructure/persistence/config/RepositoryProducer.java` (if needed)

**Directory structure:**
```
be/lutske/leolegacy/infrastructure/persistence/
├── adapter/
│   ├── JpaRecipeRepositoryAdapter.java
│   └── JpaCategoryRepositoryAdapter.java
├── mapper/
│   ├── RecipeEntityMapper.java
│   └── CategoryEntityMapper.java
├── exception/
│   └── PersistenceExceptionTranslator.java
├── config/
│   └── RepositoryProducer.java
├── entity/
│   └── (existing JPA entities)
└── repository/
    └── (existing Panache repositories)
```

## Acceptance Criteria

**Given** JpaRecipeRepositoryAdapter is implemented  
**When** a domain repository method is called  
**Then** it should delegate to the appropriate Panache repository operation

**Given** RecipeEntityMapper is implemented  
**When** a Recipe domain entity is mapped to RecipeEntity  
**Then** all properties should be correctly converted including value objects

**Given** a Recipe is saved through the adapter  
**When** the save operation completes  
**Then** the returned Recipe should have updated timestamps and persisted ID

**Given** a repository operation fails with a JPA exception  
**When** the exception occurs  
**Then** it should be translated to an appropriate domain exception

**Given** adapters are configured for dependency injection  
**When** use cases request repository dependencies  
**Then** the adapters should be injected correctly

**Given** a RecipeId is used to find a recipe  
**When** the recipe exists in the database  
**Then** the adapter should return the correct Recipe domain entity

**Given** a Recipe with ingredients and instructions is saved  
**When** it is retrieved later  
**Then** all ingredients and instructions should be preserved correctly

**Given** adapters are implemented  
**When** they are reviewed for dependencies  
**Then** they should only depend on infrastructure concerns, not domain logic

## Testing Requirements

**Unit Tests:**

Create `JpaRecipeRepositoryAdapterTest.java`:
- Test all repository method implementations
- Test entity mapping accuracy
- Test exception translation
- Mock Panache repository dependencies
- Verify domain entity conversion

Create `JpaCategoryRepositoryAdapterTest.java`:
- Test all repository method implementations
- Test entity mapping accuracy
- Test exception translation
- Mock Panache repository dependencies

Create `RecipeEntityMapperTest.java`:
- Test domain-to-entity mapping
- Test entity-to-domain mapping
- Test ingredient and instruction handling
- Test timestamp handling
- Test null value handling

Create `CategoryEntityMapperTest.java`:
- Test domain-to-entity mapping
- Test entity-to-domain mapping
- Test timestamp handling
- Test null value handling

Create `PersistenceExceptionTranslatorTest.java`:
- Test all exception translation scenarios
- Test error message preservation
- Test exception cause chaining
- Verify appropriate domain exception types

**Integration Tests:**

Create `RepositoryAdapterIntegrationTest.java`:
- Test adapters with real database
- Test transaction behavior
- Test data persistence and retrieval
- Test concurrent access scenarios
- Verify performance characteristics

**Contract Tests:**

Create `RecipeRepositoryContractTest.java`:
- Extend abstract contract test from domain layer
- Verify adapter implements all contract requirements
- Test with real database operations
- Validate business rule enforcement

Create `CategoryRepositoryContractTest.java`:
- Extend abstract contract test from domain layer
- Verify adapter implements all contract requirements
- Test with real database operations

**Test Coverage Requirements:**
- All adapter methods must have unit tests
- All mapping scenarios must be tested
- All exception translation paths must be covered
- Integration tests must verify database operations
- Contract tests must validate interface compliance