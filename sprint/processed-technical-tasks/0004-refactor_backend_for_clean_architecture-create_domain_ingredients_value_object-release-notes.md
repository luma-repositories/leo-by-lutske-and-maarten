## Summary
Created an Ingredients value object to encapsulate a collection of Ingredient objects, replacing the pipe-separated string approach with a type-safe, validated collection.

## Changes
- Created `backend/src/main/java/be/lutske/leolegacy/domain/valueobject/Ingredients.java` as a Java record
- Added validation for non-null ingredient lists
- Implemented defensive copying of input lists
- Added size(), isEmpty(), and stream() methods for collection access
- Ensured immutability through record pattern

## Impact
- Provides type-safe collection handling for ingredients
- Enforces validation at the domain layer
- Enables easier future enhancements (e.g., batch operations)
- Aligns with clean architecture principles

## Verification
- Ran `./gradlew test` which completed successfully with no failures
- Validated constructor behavior for:
  * Valid ingredient lists
  * Empty ingredient lists
  * Null lists (throws IllegalArgumentException)
- Confirmed size() and isEmpty() return correct values
- Verified stream() provides access to ingredients

## Follow-ups
- Implement integration tests with recipe entities
- Add database mapping for Ingredients collection
- Consider adding batch validation rules