## Summary
Create GetRecipeUseCase application service to retrieve a recipe by ID, following clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/application/usecase/GetRecipeUseCase.java
- backend/src/test/java/be/lutske/leolegacy/application/usecase/GetRecipeUseCaseTest.java

## Impact
Adds an application-layer use case for recipe retrieval with proper domain exception handling and dependency injection.

## Verification
- Created GetRecipeUseCase with RecipeRepository integration
- Added unit tests for success and failure scenarios
- Verified compilation after resolving dependencies

## Follow-ups
- Implement repository interface in infrastructure layer
- Add REST API integration in interface adapter layer