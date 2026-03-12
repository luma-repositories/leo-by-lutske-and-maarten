## Summary
Created Category domain entity with CategoryId and CategoryName value objects, implementing business logic and clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/entity/Category.java
- backend/src/main/java/be/lutske/leolegacy/domain/valueobject/CategoryName.java

## Impact
Replaced JPA-based CategoryEntity with a pure Java domain entity, improving separation of concerns and encapsulation.

## Verification
./gradlew clean build

## Follow-ups
- Implement unit tests for Category entity
- Create infrastructure layer mappings for Category
- Update use cases to use new domain entity