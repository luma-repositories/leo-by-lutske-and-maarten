## Summary
Create ListRecipesUseCase application service to retrieve all recipes, following clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/application/usecase/ListRecipesUseCase.java
- backend/src/test/java/be/lutske/leolegacy/application/usecase/ListRecipesUseCaseTest.java

## Impact
Adds an application-layer use case for listing all recipes using the domain repository interface.

## Verification
- Created ListRecipesUseCase with RecipeRepository integration
- Added unit tests for success and empty list scenarios
- Verified compilation after resolving dependencies

## Follow-ups
- Implement repository interface in infrastructure layer
- Add REST API integration in interface adapter layer