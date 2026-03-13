## Summary
Created domain-to-DTO mappers for Recipe and Category entities as part of Clean Architecture refactoring

## Changes
- Added RecipeMapper and CategoryMapper in interfaceadapter.rest.mapper
- Split DTO classes into individual files (RecipeDetailResponse, RecipeSummaryResponse, CategoryResponse)
- Fixed Category.java to include getDescription() method
- Updated RecipeEntity.java with proper constructor

## Impact
- Improved separation of concerns between domain and interface layers
- Enabled proper data transformation between layers
- Resolved LSP errors related to DTO class structure

## Verification
- Ran ./gradlew clean build successfully
- Verified mapper methods compile and type-check

## Follow-ups
- Add unit tests for mappers
- Update use cases to use new mappers
- Add validation logic for ingredient parsing