# Refactor REST controllers to use use cases

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Refactor existing REST controllers to delegate business logic to use cases, removing direct repository access and business logic from the interface adapter layer.

## Scope

- Refactor RecipeResource to use recipe use cases
- Refactor CategoryResource to use category use cases  
- Refactor RecipeImportResource to use import use case
- Remove direct repository dependencies from controllers
- Remove business logic and validation from controllers
- Maintain existing REST API contracts and behavior
- Update error handling to translate use case exceptions to HTTP responses

## Out of Scope

- Changes to REST API contracts or endpoints
- Use case implementation (already handled)
- Repository implementation changes (separate task)
- Transaction management changes (separate task)

## Implementation Details

### RecipeResource Refactoring

**Current file:** `be.lutske.leolegacy.interfaceadapter.rest.RecipeResource`

**Changes required:**

**Dependencies to replace:**
- Remove `RecipeRepository recipeRepository`
- Remove `CategoryRepository categoryRepository`
- Add `CreateRecipeUseCase createRecipeUseCase`
- Add `UpdateRecipeUseCase updateRecipeUseCase`
- Add `DeleteRecipeUseCase deleteRecipeUseCase`
- Add `GetRecipeUseCase getRecipeUseCase`
- Add `SearchRecipesUseCase searchRecipesUseCase`

**Method refactoring:**

`GET /recipes` → delegate to `SearchRecipesUseCase`
- Convert query parameters to SearchRecipesQuery
- Execute use case and return RecipeResponse list
- Remove direct repository access

`GET /recipes/{id}` → delegate to `GetRecipeUseCase`
- Convert path parameter to UUID
- Execute use case and return RecipeResponse
- Handle RecipeNotFoundException → 404 response

`POST /recipes` → delegate to `CreateRecipeUseCase`
- Convert CreateRecipeRequest to CreateRecipeCommand
- Execute use case and return RecipeResponse
- Handle InvalidRecipeDataException → 400 response

`PUT /recipes/{id}` → delegate to `UpdateRecipeUseCase`
- Convert request body and path parameter to UpdateRecipeCommand
- Execute use case and return RecipeResponse
- Handle RecipeNotFoundException → 404 response
- Handle InvalidRecipeDataException → 400 response

`DELETE /recipes/{id}` → delegate to `DeleteRecipeUseCase`
- Convert path parameter to UUID
- Execute use case
- Handle RecipeNotFoundException → 404 response
- Return 204 No Content on success

**Remove business logic:**
- Remove validation logic from controller methods
- Remove transaction annotations (@Transactional)
- Remove direct entity manipulation
- Remove repository query logic

### CategoryResource Refactoring

**Current file:** `be.lutske.leolegacy.interfaceadapter.rest.CategoryResource`

**Changes required:**

**Dependencies to replace:**
- Remove `CategoryRepository categoryRepository`
- Add `GetAllCategoriesUseCase getAllCategoriesUseCase`
- Add `CreateCategoryUseCase createCategoryUseCase` (if needed)

**Method refactoring:**

`GET /categories` → delegate to `GetAllCategoriesUseCase`
- Execute use case and return CategoryResponse list
- Remove direct repository access

### RecipeImportResource Refactoring

**Current file:** `be.lutske.leolegacy.interfaceadapter.rest.RecipeImportResource`

**Changes required:**

**Dependencies to replace:**
- Remove `RecipeExtractionService recipeExtractionService`
- Remove `RecipeRepository recipeRepository`
- Add `ImportRecipeFromImageUseCase importRecipeFromImageUseCase`

**Method refactoring:**

`POST /recipes/import` → delegate to `ImportRecipeFromImageUseCase`
- Convert ImportRecipeRequest to ImportRecipeCommand
- Execute use case and return RecipeResponse
- Handle RecipeImportException → 400 response
- Handle AI extraction failures → 422 response

**Remove business logic:**
- Remove direct AI service calls
- Remove recipe creation logic
- Remove transaction annotations
- Remove repository save operations

### Error Handling Strategy

**Exception Translation:**

Create `be.lutske.leolegacy.interfaceadapter.rest.exception.RestExceptionHandler`:

**Use Case Exception → HTTP Response mapping:**
- `RecipeNotFoundException` → 404 Not Found
- `CategoryNotFoundException` → 404 Not Found  
- `InvalidRecipeDataException` → 400 Bad Request
- `RecipeImportException` → 422 Unprocessable Entity
- `UseCaseException` → 500 Internal Server Error

**Error Response Format:**
```json
{
  "error": "RECIPE_NOT_FOUND",
  "message": "Recipe with ID 123e4567-e89b-12d3-a456-426614174000 not found",
  "timestamp": "2026-03-12T10:30:00Z"
}
```

### Request/Response Mapping

**Create mapping utilities:**

Create `be.lutske.leolegacy.interfaceadapter.rest.mapper.RecipeMapper`:
- `CreateRecipeCommand fromRequest(CreateRecipeRequest request)`
- `UpdateRecipeCommand fromRequest(UUID id, UpdateRecipeRequest request)`
- `SearchRecipesQuery fromQueryParams(String title, UUID categoryId, Integer limit, Integer offset)`
- `RecipeResponse toResponse(RecipeResponse useCaseResponse)`

Create `be.lutske.leolegacy.interfaceadapter.rest.mapper.ImportMapper`:
- `ImportRecipeCommand fromRequest(ImportRecipeRequest request)`

## Files / Modules Impacted

**Files to modify:**
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`

**New files to create:**
- `be/lutske/leolegacy/interfaceadapter/rest/exception/RestExceptionHandler.java`
- `be/lutske/leolegacy/interfaceadapter/rest/mapper/RecipeMapper.java`
- `be/lutske/leolegacy/interfaceadapter/rest/mapper/ImportMapper.java`

**Directory structure:**
```
be/lutske/leolegacy/interfaceadapter/rest/
├── exception/
│   └── RestExceptionHandler.java
├── mapper/
│   ├── RecipeMapper.java
│   └── ImportMapper.java
├── RecipeResource.java (modified)
├── CategoryResource.java (modified)
└── RecipeImportResource.java (modified)
```

## Acceptance Criteria

**Given** RecipeResource is refactored  
**When** a GET /recipes request is made  
**Then** it should delegate to SearchRecipesUseCase and return the same response format

**Given** RecipeResource is refactored  
**When** a POST /recipes request is made with valid data  
**Then** it should delegate to CreateRecipeUseCase and return 201 with recipe data

**Given** RecipeResource is refactored  
**When** a POST /recipes request is made with invalid data  
**Then** it should return 400 with validation error details

**Given** RecipeResource is refactored  
**When** a GET /recipes/{id} request is made for non-existent recipe  
**Then** it should return 404 with appropriate error message

**Given** RecipeImportResource is refactored  
**When** a POST /recipes/import request is made  
**Then** it should delegate to ImportRecipeFromImageUseCase and maintain existing behavior

**Given** controllers are refactored  
**When** they are reviewed for dependencies  
**Then** they should only depend on use cases, not repositories or domain services

**Given** controllers are refactored  
**When** they are reviewed for business logic  
**Then** they should contain no validation, business rules, or transaction management

**Given** error handling is implemented  
**When** use case exceptions are thrown  
**Then** they should be translated to appropriate HTTP status codes and error responses

**Given** all controllers are refactored  
**When** existing API tests are run  
**Then** all tests should pass without modification

## Testing Requirements

**Unit Tests:**

Create `RecipeResourceTest.java`:
- Test all endpoint methods with mocked use cases
- Test request/response mapping accuracy
- Test error handling and HTTP status codes
- Verify use case method calls and parameters
- Test query parameter handling

Create `CategoryResourceTest.java`:
- Test category listing with mocked use case
- Test response mapping accuracy
- Verify use case integration

Create `RecipeImportResourceTest.java`:
- Test import endpoint with mocked use case
- Test error handling for import failures
- Test request mapping for import commands
- Verify AI extraction delegation

Create `RestExceptionHandlerTest.java`:
- Test exception translation to HTTP responses
- Test error response format consistency
- Test all supported exception types
- Verify HTTP status code mapping

**Integration Tests:**

Create `RecipeResourceIntegrationTest.java`:
- Test complete request/response flow
- Test with real use case implementations
- Verify transaction boundaries
- Test error scenarios end-to-end

**API Contract Tests:**

Create `RecipeApiContractTest.java`:
- Verify existing API contracts remain unchanged
- Test request/response schemas
- Test HTTP status codes for all scenarios
- Ensure backward compatibility

**Test Coverage Requirements:**
- All controller methods must have unit tests
- All error handling paths must be tested
- Request/response mapping must be verified
- Integration tests must cover complete flows
- API contract tests must ensure no breaking changes