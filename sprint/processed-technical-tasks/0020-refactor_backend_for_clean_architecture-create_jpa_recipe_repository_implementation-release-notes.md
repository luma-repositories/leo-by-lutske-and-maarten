## Summary
Create JPA-based implementation of RecipeRepository interface in infrastructure layer

## Changes
- backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/JpaRecipeRepository.java

## Impact
Provides persistence implementation for recipe domain entities while maintaining clean architecture boundaries

## Verification
- ./gradlew clean build
- ./gradlew test

## Follow-ups
- Implement REST API integration for recipe operations
- Add integration tests with real database