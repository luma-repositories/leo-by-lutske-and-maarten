## Summary
Created `CategoryName` value object in domain layer to encapsulate and validate category names.

## Changes
- Created `src/main/java/domain/CategoryName.java`
- Added validation for non-null, non-empty, and max 100 characters

## Impact
- Replaces primitive String usage with typed value object
- Enforces validation at construction time
- Follows clean architecture principles by isolating domain logic

## Verification
- Ran `./gradlew build` successfully
- No test failures observed
- Code compiles cleanly

## Follow-ups
- Update existing code to use `CategoryName` instead of raw Strings (handled in later tasks)