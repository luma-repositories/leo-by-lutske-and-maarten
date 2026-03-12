## Summary
Created a CategoryId value object to encapsulate category identifiers with validation, replacing primitive String usage with a type-safe, immutable structure.

## Changes
- Created `backend/src/main/java/be/lutske/leolegacy/domain/valueobject/CategoryId.java` as a Java record
- Added validation for non-null and non-empty category IDs
- Implemented value() method for controlled access
- Ensured immutability through record pattern

## Impact
- Provides type-safe handling of category identifiers
- Enforces validation at the domain layer
- Enables easier future enhancements (e.g., database mapping)
- Aligns with clean architecture principles

## Verification
- Ran `./gradlew test` which completed successfully with no failures
- Validated constructor behavior for:
  * Valid category IDs
  * Null input (throws IllegalArgumentException)
  * Empty string input (throws IllegalArgumentException)
- Confirmed value() method returns correct ID

## Follow-ups
- Implement integration tests with category entities
- Add database mapping for CategoryId
- Consider adding ID formatting/parsing logic in infrastructure layer