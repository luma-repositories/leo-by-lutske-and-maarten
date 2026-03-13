## Summary
Create domain RecipeRepository interface for clean architecture

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/repository/RecipeRepository.java

## Impact
Enables repository pattern implementation in domain layer

## Verification
./gradlew clean build

## Follow-ups
- Implement repository interface in infrastructure layer