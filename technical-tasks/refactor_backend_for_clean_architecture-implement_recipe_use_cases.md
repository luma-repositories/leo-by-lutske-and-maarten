# Implement use cases for recipe operations

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create use case interactors that encapsulate business logic and orchestrate domain operations, providing clear boundaries between application logic and infrastructure concerns.

## Scope

- Create use cases for recipe CRUD operations
- Create use case for recipe import from AI extraction
- Create use case for recipe search and filtering
- Define input/output DTOs for use case boundaries
- Implement business logic orchestration and validation
- Establish transaction boundaries at use case level

## Out of Scope

- Domain entity implementation (already handled)
- Repository interface implementation (separate task)
- REST controller modifications (separate task)
- AI service implementation changes (separate task)

## Implementation Details

### Use Case Input/Output DTOs

Create `be.lutske.leolegacy.application.usecase.dto` package with:

**CreateRecipeCommand:**
- `String title` (required)
- `String description` (optional)
- `List<CreateIngredientCommand> ingredients` (required, min 1)
- `List<CreateInstructionCommand> instructions` (required, min 1)
- `UUID categoryId` (optional)

**UpdateRecipeCommand:**
- `UUID recipeId` (required)
- `String title` (optional)
- `String description` (optional)
- `List<CreateIngredientCommand> ingredients` (optional)
- `List<CreateInstructionCommand> instructions` (optional)
- `UUID categoryId` (optional)

**ImportRecipeCommand:**
- `String imageBase64` (required)
- `UUID categoryId` (optional)

**SearchRecipesQuery:**
- `String titleFragment` (optional)
- `UUID categoryId` (optional)
- `int limit` (default 50, max 100)
- `int offset` (default 0)

**RecipeResponse:**
- `UUID id`
- `String title`
- `String description`
- `List<IngredientResponse> ingredients`
- `List<InstructionResponse> instructions`
- `UUID categoryId`
- `String categoryName`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

### Recipe Use Cases

Create `be.lutske.leolegacy.application.usecase.CreateRecipeUseCase`:

**Dependencies:**
- `RecipeRepository recipeRepository`
- `CategoryRepository categoryRepository`

**Method:** `RecipeResponse execute(CreateRecipeCommand command)`

**Business Logic:**
1. Validate command parameters
2. Verify category exists if provided
3. Create Recipe domain entity with validation
4. Save recipe through repository
5. Return RecipeResponse with created data

**Validation Rules:**
- Title must be provided and valid
- At least one ingredient required
- At least one instruction required
- Category must exist if provided
- Instructions must have valid sequence numbers

Create `be.lutske.leolegacy.application.usecase.UpdateRecipeUseCase`:

**Dependencies:**
- `RecipeRepository recipeRepository`
- `CategoryRepository categoryRepository`

**Method:** `RecipeResponse execute(UpdateRecipeCommand command)`

**Business Logic:**
1. Validate command parameters
2. Find existing recipe by ID
3. Verify category exists if being changed
4. Update recipe domain entity with validation
5. Save updated recipe through repository
6. Return RecipeResponse with updated data

Create `be.lutske.leolegacy.application.usecase.DeleteRecipeUseCase`:

**Dependencies:**
- `RecipeRepository recipeRepository`

**Method:** `void execute(UUID recipeId)`

**Business Logic:**
1. Validate recipe ID parameter
2. Verify recipe exists
3. Delete recipe through repository

Create `be.lutske.leolegacy.application.usecase.GetRecipeUseCase`:

**Dependencies:**
- `RecipeRepository recipeRepository`

**Method:** `Optional<RecipeResponse> execute(UUID recipeId)`

**Business Logic:**
1. Validate recipe ID parameter
2. Find recipe by ID through repository
3. Convert to RecipeResponse if found
4. Return Optional result

Create `be.lutske.leolegacy.application.usecase.SearchRecipesUseCase`:

**Dependencies:**
- `RecipeRepository recipeRepository`
- `CategoryRepository categoryRepository`

**Method:** `List<RecipeResponse> execute(SearchRecipesQuery query)`

**Business Logic:**
1. Validate query parameters
2. Apply search filters based on query
3. Retrieve matching recipes through repository
4. Convert to RecipeResponse list
5. Apply pagination limits

### Recipe Import Use Case

Create `be.lutske.leolegacy.application.usecase.ImportRecipeFromImageUseCase`:

**Dependencies:**
- `RecipeExtractionService extractionService`
- `RecipeRepository recipeRepository`
- `CategoryRepository categoryRepository`

**Method:** `RecipeResponse execute(ImportRecipeCommand command)`

**Business Logic:**
1. Validate image data and format
2. Extract recipe data using AI service
3. Validate extracted recipe data
4. Verify category exists if provided
5. Create Recipe domain entity from extraction
6. Save recipe through repository
7. Return RecipeResponse with imported data

**Error Handling:**
- Handle AI extraction failures gracefully
- Validate extracted data meets domain requirements
- Provide meaningful error messages for import failures

### Use Case Exception Handling

Create `be.lutske.leolegacy.application.usecase.exception` package with:

**UseCaseException** (base class)
**RecipeNotFoundException**
**CategoryNotFoundException**
**InvalidRecipeDataException**
**RecipeImportException**

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/application/usecase/dto/CreateRecipeCommand.java`
- `be/lutske/leolegacy/application/usecase/dto/UpdateRecipeCommand.java`
- `be/lutske/leolegacy/application/usecase/dto/ImportRecipeCommand.java`
- `be/lutske/leolegacy/application/usecase/dto/SearchRecipesQuery.java`
- `be/lutske/leolegacy/application/usecase/dto/RecipeResponse.java`
- `be/lutske/leolegacy/application/usecase/dto/IngredientResponse.java`
- `be/lutske/leolegacy/application/usecase/dto/InstructionResponse.java`
- `be/lutske/leolegacy/application/usecase/CreateRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/UpdateRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/DeleteRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/GetRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/SearchRecipesUseCase.java`
- `be/lutske/leolegacy/application/usecase/ImportRecipeFromImageUseCase.java`
- `be/lutske/leolegacy/application/usecase/exception/UseCaseException.java`
- `be/lutske/leolegacy/application/usecase/exception/RecipeNotFoundException.java`
- `be/lutske/leolegacy/application/usecase/exception/CategoryNotFoundException.java`
- `be/lutske/leolegacy/application/usecase/exception/InvalidRecipeDataException.java`
- `be/lutske/leolegacy/application/usecase/exception/RecipeImportException.java`

**Directory structure:**
```
be/lutske/leolegacy/
├── application/
│   ├── service/
│   │   └── (existing services)
│   └── usecase/
│       ├── dto/
│       │   ├── CreateRecipeCommand.java
│       │   ├── UpdateRecipeCommand.java
│       │   ├── ImportRecipeCommand.java
│       │   ├── SearchRecipesQuery.java
│       │   ├── RecipeResponse.java
│       │   ├── IngredientResponse.java
│       │   └── InstructionResponse.java
│       ├── exception/
│       │   ├── UseCaseException.java
│       │   ├── RecipeNotFoundException.java
│       │   ├── CategoryNotFoundException.java
│       │   ├── InvalidRecipeDataException.java
│       │   └── RecipeImportException.java
│       ├── CreateRecipeUseCase.java
│       ├── UpdateRecipeUseCase.java
│       ├── DeleteRecipeUseCase.java
│       ├── GetRecipeUseCase.java
│       ├── SearchRecipesUseCase.java
│       └── ImportRecipeFromImageUseCase.java
```

## Acceptance Criteria

**Given** a CreateRecipeUseCase is implemented  
**When** it is executed with valid command data  
**Then** it should create a new recipe and return RecipeResponse

**Given** a CreateRecipeUseCase is implemented  
**When** it is executed with invalid command data  
**Then** it should throw InvalidRecipeDataException with validation details

**Given** an UpdateRecipeUseCase is implemented  
**When** it is executed with valid command data  
**Then** it should update the existing recipe and return updated RecipeResponse

**Given** an UpdateRecipeUseCase is implemented  
**When** it is executed with non-existent recipe ID  
**Then** it should throw RecipeNotFoundException

**Given** a DeleteRecipeUseCase is implemented  
**When** it is executed with existing recipe ID  
**Then** it should remove the recipe from the repository

**Given** a GetRecipeUseCase is implemented  
**When** it is executed with existing recipe ID  
**Then** it should return Optional containing RecipeResponse

**Given** a SearchRecipesUseCase is implemented  
**When** it is executed with search criteria  
**Then** it should return filtered list of RecipeResponse objects

**Given** an ImportRecipeFromImageUseCase is implemented  
**When** it is executed with valid image data  
**Then** it should extract recipe data and create new recipe

**Given** use cases are implemented  
**When** they are reviewed for dependencies  
**Then** they should only depend on domain repositories and services

**Given** use cases are implemented  
**When** they are examined for transaction boundaries  
**Then** they should be annotated with @Transactional at appropriate levels

## Testing Requirements

**Unit Tests:**

Create `CreateRecipeUseCaseTest.java`:
- Test successful recipe creation with valid data
- Test validation failures with invalid data
- Test category verification logic
- Mock repository dependencies
- Verify domain entity creation and saving

Create `UpdateRecipeUseCaseTest.java`:
- Test successful recipe updates
- Test recipe not found scenarios
- Test partial update scenarios
- Test category change validation
- Mock repository dependencies

Create `DeleteRecipeUseCaseTest.java`:
- Test successful recipe deletion
- Test recipe not found scenarios
- Mock repository dependencies

Create `GetRecipeUseCaseTest.java`:
- Test successful recipe retrieval
- Test recipe not found scenarios
- Test response mapping accuracy
- Mock repository dependencies

Create `SearchRecipesUseCaseTest.java`:
- Test search with various criteria
- Test pagination behavior
- Test empty result scenarios
- Mock repository dependencies

Create `ImportRecipeFromImageUseCaseTest.java`:
- Test successful recipe import
- Test AI extraction failures
- Test invalid extracted data handling
- Test category assignment
- Mock all dependencies

**Integration Tests:**

Create `RecipeUseCaseIntegrationTest.java`:
- Test use case interactions with real repositories
- Test transaction behavior
- Test error handling across layers
- Verify end-to-end use case execution

**Test Coverage Requirements:**
- All use case methods must have unit tests
- All validation logic must be tested
- All error scenarios must be covered
- Mock dependencies must be properly verified
- Integration tests must verify transaction boundaries