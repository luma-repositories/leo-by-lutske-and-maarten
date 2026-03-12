## Summary
Created `ViewCount` value object in domain layer to encapsulate and validate recipe view counts.

## Changes
- Created `src/main/java/domain/ViewCount.java`
- Added validation for non-negative values
- Included increment behavior

## Impact
- Replaces primitive int usage with typed value object
- Enforces validation at construction time
- Follows clean architecture principles by isolating domain logic

## Verification
- Ran `./gradlew build` successfully
- No test failures observed
- Code compiles cleanly

## Follow-ups
- Update existing code to use `ViewCount` instead of raw ints (handled in later tasks)