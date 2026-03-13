## Summary
Create domain RecipeNotFoundException to represent business exception when a recipe cannot be found, following clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/exception/RecipeNotFoundException.java
- backend/src/test/java/be/lutske/leolegacy/domain/exception/RecipeNotFoundExceptionTest.java

## Impact
Introduces a domain-level exception for recipe not found scenarios, with proper error messaging and RecipeId tracking.

## Verification
- Created RecipeNotFoundException with RecipeId integration
- Added unit tests for exception behavior
- Verified compilation after creating RecipeId dependency

## Follow-ups
- Ensure HTTP status mapping in interface adapter layer
- Verify exception handling in infrastructure layer