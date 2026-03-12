## Summary
Created an Ingredient value object in the domain layer to replace the pipe-separated string approach for recipe ingredients, improving structure and validation.

## Changes
- Created `backend/src/main/java/be/lutske/leolegacy/domain/valueobject/Ingredient.java` as a Java record with name, amount, and unit fields
- Added validation for non-null/non-empty ingredient names
- Implemented accessor methods for all fields
- Ensured immutability through record pattern

## Impact
- Improves data integrity for ingredient representation
- Provides a type-safe alternative to string-based ingredients
- Enables easier future enhancements (unit conversion, validation)
- Aligns with clean architecture principles

## Verification
- Ran `./gradlew test` which completed successfully with no failures
- Validated constructor behavior for:
  * Valid ingredients with all fields
  * Valid ingredients with only name
  * Null name (throws IllegalArgumentException)
  * Empty name (throws IllegalArgumentException)
- Confirmed accessor methods return correct values

## Follow-ups
- Consider adding unit conversion logic in future tasks
- Implement database mapping for Ingredient value object
- Add integration tests with recipe entities