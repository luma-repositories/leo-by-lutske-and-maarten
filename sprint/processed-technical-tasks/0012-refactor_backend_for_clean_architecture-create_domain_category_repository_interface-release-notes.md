## Summary
Create domain CategoryRepository interface for clean architecture

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/repository/CategoryRepository.java

## Impact
Enables repository pattern implementation for categories in domain layer

## Verification
./gradlew clean build

## Follow-ups
- Implement repository interface in infrastructure layer