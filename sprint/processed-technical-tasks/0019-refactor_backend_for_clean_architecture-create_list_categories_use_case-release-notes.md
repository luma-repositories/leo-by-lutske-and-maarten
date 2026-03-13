## Summary
Create ListCategoriesUseCase application service to retrieve all categories, following clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/application/usecase/ListCategoriesUseCase.java
- backend/src/test/java/be/lutske/leolegacy/application/usecase/ListCategoriesUseCaseTest.java

## Impact
Adds an application-layer use case for listing all categories using the domain repository interface.

## Verification
- Created ListCategoriesUseCase with CategoryRepository integration
- Added unit tests for success and empty list scenarios
- Verified compilation after resolving dependencies

## Follow-ups
- Implement repository interface in infrastructure layer
- Add REST API integration in interface adapter layer