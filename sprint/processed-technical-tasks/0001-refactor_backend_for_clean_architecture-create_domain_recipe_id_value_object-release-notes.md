## Summary
Created RecipeId value object for domain layer to encapsulate recipe identifiers with validation

## Changes
- Added `RecipeId` Java record in domain layer
- Implemented null/empty string validation
- Created corresponding unit tests

## Impact
- Replaces primitive String usage with typed value object
- Enforces validation at domain layer boundaries
- No breaking changes to existing code (integration handled in later tasks)

## Verification
- All unit tests passed successfully
- Built successfully with ./gradlew build
- Validation logic correctly rejects null/empty values

## Follow-ups
- Update existing code to use RecipeId instead of raw Strings (next task)
- Implement database mapping for RecipeId (infrastructure task)