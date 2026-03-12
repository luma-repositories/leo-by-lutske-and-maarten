## Summary
Created Recipe domain entity with value objects (RecipeId, RecipeTitle, Ingredients, PreparationSteps, ViewCount) and Category entity, implementing business logic and clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/entity/Recipe.java
- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/ViewCount.java

## Impact
Replaced JPA-based RecipeEntity with a pure Java domain entity, improving separation of concerns and encapsulation.

## Verification
./gradlew clean build

## Follow-ups
- Implement unit tests for Recipe entity
- Create infrastructure layer mappings for Recipe
- Update use cases to use new domain entity