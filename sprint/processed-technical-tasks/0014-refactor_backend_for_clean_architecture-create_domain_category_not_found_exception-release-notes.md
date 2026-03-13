## Summary
Create domain CategoryNotFoundException to represent business exception when a category cannot be found, following clean architecture principles.

## Changes
- backend/src/main/java/be/lutske/leolegacy/domain/exception/CategoryNotFoundException.java
- backend/src/test/java/be/lutske/leolegacy/domain/exception/CategoryNotFoundExceptionTest.java

## Impact
Introduces a domain-level exception for category not found scenarios, with proper error messaging and CategoryId tracking.

## Verification
- Created CategoryNotFoundException with CategoryId integration
- Added unit tests for exception behavior
- Verified compilation after creating CategoryId dependency

## Follow-ups
- Ensure HTTP status mapping in interface adapter layer
- Verify exception handling in infrastructure layer