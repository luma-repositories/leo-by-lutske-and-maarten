## Summary
Created RecipeTitle value object in domain layer with validation for non-null, non-empty, and maximum length (255 chars).

## Changes
- Added `backend/src/main/java/be/lutske/leolegacy/domain/valueobject/RecipeTitle.java`
- Added `backend/src/test/java/be/lutske/leolegacy/domain/valueobject/RecipeTitleTest.java`

## Impact
Enforces consistent recipe title handling with validation across the domain layer. No breaking changes to existing code.

## Verification
- Built project successfully with `./gradlew clean build`
- Ran unit tests with `./gradlew test` (all tests passed)

## Follow-ups
- Implement usage in recipe entity (task 0003)
- Add database mapping (infrastructure task)