## Summary
Created a PreparationSteps value object to encapsulate recipe preparation instructions with validation, replacing the previous string-based approach with a type-safe, immutable structure.

## Changes
- Created `backend/src/main/java/be/lutske/leolegacy/domain/valueobject/PreparationSteps.java` as a Java record
- Added validation for non-null and non-empty preparation steps
- Implemented value() method for controlled access
- Ensured immutability through record pattern

## Impact
- Provides type-safe handling of preparation instructions
- Enforces validation at the domain layer
- Enables easier future enhancements (e.g., step parsing/formatting)
- Aligns with clean architecture principles

## Verification
- Ran `./gradlew test` which completed successfully with no failures
- Validated constructor behavior for:
  * Valid preparation steps
  * Null input (throws IllegalArgumentException)
  * Empty string input (throws IllegalArgumentException)
- Confirmed value() method returns correct instructions

## Follow-ups
- Implement integration tests with recipe entities
- Add database mapping for PreparationSteps
- Consider adding step parsing/formatting logic in infrastructure layer