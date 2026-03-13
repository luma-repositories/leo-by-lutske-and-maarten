## Summary
Create JPA-based implementation of CategoryRepository interface in infrastructure layer

## Changes
- backend/src/main/java/be/lutske/leolegacy/infrastructure/persistence/JpaCategoryRepository.java

## Impact
Provides persistence implementation for category domain entities while maintaining clean architecture boundaries

## Verification
- ./gradlew clean build
- ./gradlew test

## Follow-ups
- Implement REST API integration for category operations
- Add integration tests with real database