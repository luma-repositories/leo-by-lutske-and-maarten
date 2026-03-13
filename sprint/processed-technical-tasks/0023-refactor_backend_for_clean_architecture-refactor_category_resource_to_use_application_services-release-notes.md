## Summary
Refactor CategoryResource to use application services instead of direct repository access

## Changes
- backend/src/main/java/be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java

## Impact
Decouples REST layer from infrastructure layer by using application services for business logic

## Verification
- ./gradlew clean build
- ./gradlew test

## Follow-ups
- Add unit tests for REST endpoints with mocked use cases
- Implement exception mapping for domain exceptions