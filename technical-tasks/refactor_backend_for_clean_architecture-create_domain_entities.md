# Create domain entities for core business models

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create pure domain entities that represent core business concepts without infrastructure dependencies, separating business rules from persistence concerns.

## Scope

- Create Recipe domain entity with business rules and validation
- Create Category domain entity with business behavior
- Create RecipeIngredient value object for ingredient modeling
- Create RecipeInstruction value object for instruction modeling
- Define domain-specific exceptions for business rule violations
- Implement domain validation logic independent of JPA

## Out of Scope

- JPA entity modifications (will be handled in separate task)
- Repository interface creation (separate task)
- Use case implementation (separate task)
- Database schema changes

## Implementation Details

### Recipe Domain Entity

Create `be.lutske.leolegacy.domain.model.Recipe` with:

**Properties:**
- `RecipeId id` (value object for type safety)
- `String title` (required, 1-200 characters)
- `String description` (optional, max 2000 characters)
- `List<RecipeIngredient> ingredients` (required, min 1 ingredient)
- `List<RecipeInstruction> instructions` (required, min 1 instruction)
- `CategoryId categoryId` (optional foreign reference)
- `LocalDateTime createdAt` (immutable)
- `LocalDateTime updatedAt` (mutable)

**Business Rules:**
- Title cannot be null or empty
- Must have at least one ingredient
- Must have at least one instruction
- Instructions must be ordered (sequence numbers)
- Creation timestamp is immutable
- Update timestamp changes on any modification

**Methods:**
- `updateTitle(String newTitle)` - validates and updates title
- `updateDescription(String newDescription)` - updates description
- `addIngredient(RecipeIngredient ingredient)` - adds ingredient with validation
- `removeIngredient(int index)` - removes ingredient by index
- `addInstruction(RecipeInstruction instruction)` - adds instruction with sequence
- `removeInstruction(int index)` - removes instruction and reorders
- `assignToCategory(CategoryId categoryId)` - assigns recipe to category
- `markAsUpdated()` - updates the updatedAt timestamp

### Category Domain Entity

Create `be.lutske.leolegacy.domain.model.Category` with:

**Properties:**
- `CategoryId id` (value object)
- `String name` (required, unique, 1-100 characters)
- `String description` (optional, max 500 characters)
- `LocalDateTime createdAt` (immutable)

**Business Rules:**
- Name cannot be null, empty, or only whitespace
- Name must be unique across all categories
- Name is case-insensitive for uniqueness
- Creation timestamp is immutable

**Methods:**
- `updateName(String newName)` - validates and updates name
- `updateDescription(String newDescription)` - updates description

### Value Objects

Create `be.lutske.leolegacy.domain.model.RecipeIngredient` with:
- `String name` (required, 1-200 characters)
- `String quantity` (optional, max 50 characters)
- `String unit` (optional, max 20 characters)

Create `be.lutske.leolegacy.domain.model.RecipeInstruction` with:
- `int sequenceNumber` (required, positive)
- `String description` (required, 1-1000 characters)

Create `be.lutske.leolegacy.domain.model.RecipeId` with:
- `UUID value` (required, immutable)
- Static factory method `generate()` for new IDs
- Static factory method `from(UUID uuid)` for existing IDs

Create `be.lutske.leolegacy.domain.model.CategoryId` with:
- `UUID value` (required, immutable)
- Static factory method `generate()` for new IDs
- Static factory method `from(UUID uuid)` for existing IDs

### Domain Exceptions

Create `be.lutske.leolegacy.domain.exception.DomainException` (base class)
Create `be.lutske.leolegacy.domain.exception.InvalidRecipeException`
Create `be.lutske.leolegacy.domain.exception.InvalidCategoryException`

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/model/Recipe.java`
- `be/lutske/leolegacy/domain/model/Category.java`
- `be/lutske/leolegacy/domain/model/RecipeIngredient.java`
- `be/lutske/leolegacy/domain/model/RecipeInstruction.java`
- `be/lutske/leolegacy/domain/model/RecipeId.java`
- `be/lutske/leolegacy/domain/model/CategoryId.java`
- `be/lutske/leolegacy/domain/exception/DomainException.java`
- `be/lutske/leolegacy/domain/exception/InvalidRecipeException.java`
- `be/lutske/leolegacy/domain/exception/InvalidCategoryException.java`

**Directory structure:**
```
be/lutske/leolegacy/
├── domain/
│   ├── model/
│   │   ├── Recipe.java
│   │   ├── Category.java
│   │   ├── RecipeIngredient.java
│   │   ├── RecipeInstruction.java
│   │   ├── RecipeId.java
│   │   └── CategoryId.java
│   └── exception/
│       ├── DomainException.java
│       ├── InvalidRecipeException.java
│       └── InvalidCategoryException.java
```

## Acceptance Criteria

**Given** a Recipe domain entity is created  
**When** it is instantiated with valid data  
**Then** it should contain all required properties and enforce business rules

**Given** a Recipe domain entity  
**When** an invalid title is provided (null, empty, or too long)  
**Then** it should throw InvalidRecipeException

**Given** a Recipe domain entity  
**When** no ingredients are provided  
**Then** it should throw InvalidRecipeException

**Given** a Recipe domain entity  
**When** no instructions are provided  
**Then** it should throw InvalidRecipeException

**Given** a Category domain entity is created  
**When** it is instantiated with valid data  
**Then** it should contain all required properties and enforce business rules

**Given** a Category domain entity  
**When** an invalid name is provided (null, empty, or too long)  
**Then** it should throw InvalidCategoryException

**Given** RecipeId and CategoryId value objects  
**When** they are created with the same UUID  
**Then** they should be equal and have the same hash code

**Given** domain entities are created  
**When** they are inspected for dependencies  
**Then** they should have no JPA, database, or infrastructure dependencies

## Testing Requirements

**Unit Tests:**

Create `RecipeTest.java`:
- Test valid recipe creation
- Test invalid title validation
- Test ingredient management (add/remove)
- Test instruction management (add/remove/reorder)
- Test category assignment
- Test timestamp behavior
- Test business rule enforcement

Create `CategoryTest.java`:
- Test valid category creation
- Test invalid name validation
- Test name uniqueness requirements
- Test description updates

Create `ValueObjectTest.java`:
- Test RecipeIngredient creation and validation
- Test RecipeInstruction creation and validation
- Test RecipeId equality and hash code
- Test CategoryId equality and hash code

**Test Coverage Requirements:**
- All business rules must be covered by unit tests
- All validation logic must be tested with valid and invalid inputs
- All domain exceptions must be tested
- Value object equality and immutability must be verified