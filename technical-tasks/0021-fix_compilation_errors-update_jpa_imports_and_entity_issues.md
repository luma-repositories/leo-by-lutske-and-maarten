# Fix compilation errors - Update JPA imports and entity issues

## Related User Story

User Story: fix_compilation_errors

## Objective

Fix all compilation errors in the codebase by updating JPA imports from javax to jakarta, fixing missing entity methods, and resolving type mismatches.

## Scope

- Update all JPA imports from `javax.persistence` to `jakarta.persistence`
- Fix missing getter/setter methods in entity classes
- Resolve ID field type inconsistencies
- Fix repository method calls and implementations
- Ensure all entity fields are properly defined

## Out of Scope

- Business logic changes
- Database schema changes
- Adding new features
- Performance optimizations

## Implementation Details

### 1. Update JPA Imports

**Files to update:**
- `RecipeRepositoryImpl.java`
- `CategoryRepositoryImpl.java`
- `CategoryEntity.java`
- `RecipeEntity.java` (if exists)

**Change all occurrences:**
```java
// FROM:
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.*;

// TO:
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.*;
```

### 2. Fix CategoryEntity Missing Annotations

**File:** `CategoryEntity.java`

**Current issues:**
- Missing `@Entity`, `@Table`, `@Id`, `@GeneratedValue` annotations
- Missing import for `GenerationType`

**Required imports:**
```java
import jakarta.persistence.*;
```

**Required annotations:**
```java
@Entity
@Table(name = "category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ... other fields
}
```

### 3. Fix RecipeEntity Missing Methods

**File:** `RecipeEntity.java`

**Add missing getter/setter methods:**
- `getIngredients()` / `setIngredients()`
- `getInstructions()` / `setInstructions()`
- `getViewCount()` / `setViewCount()`
- `getCategory()` / `setCategory()`
- `getPreparation()` / `setPreparation()`
- `setSource()`
- `setCreatedAt()`
- `setImportMetadata()`

### 4. Fix ID Type Inconsistencies

**Problem:** Code mixes String and Long for ID fields

**Files to fix:**
- `RecipeRepositoryImpl.java` lines 73, 86
- `CategoryRepositoryImpl.java` lines 61, 68

**Solution:** Ensure consistent ID type usage:
- If entities use `Long id`, domain objects should use `Long` or convert properly
- Update `Recipe.getId()` and `Category.getId()` return types to match entity types

### 5. Fix Repository Method Issues

**File:** `RecipeRepositoryImpl.java`

**Missing methods to implement:**
- `findByCategoryId(Long categoryId)`
- `listAll()`
- `findTopByViewCount(int limit)`
- `persist(RecipeEntity entity)`

**File:** `RecipeResource.java`

**Fix method calls:**
```java
// Line 50 - Fix return type:
Optional<RecipeEntity> recipeOpt = recipeRepository.findById(id);
if (recipeOpt.isEmpty()) {
    throw new WebApplicationException(404);
}
RecipeEntity recipe = recipeOpt.get();
```

### 6. Add Missing Entity Fields

**File:** `RecipeEntity.java`

**Ensure these fields exist with proper getters/setters:**
- `preparation` (String)
- `source` (String) 
- `createdAt` (Instant)
- `importMetadata` (String)

## Files / Modules Impacted

**Files to modify:**
- `be/lutske/leolegacy/infrastructure/persistence/repository/RecipeRepositoryImpl.java`
- `be/lutske/leolegacy/infrastructure/persistence/repository/CategoryRepositoryImpl.java`
- `be/lutske/leolegacy/infrastructure/persistence/entity/CategoryEntity.java`
- `be/lutske/leolegacy/infrastructure/persistence/entity/RecipeEntity.java`
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`

## Acceptance Criteria

**Given** compilation errors exist in the codebase  
**When** all JPA imports are updated to jakarta.persistence  
**Then** no "package javax.persistence does not exist" errors should occur

**Given** entity classes are missing annotations  
**When** proper JPA annotations are added  
**Then** entities should be recognized by Hibernate

**Given** missing getter/setter methods exist  
**When** all required methods are implemented  
**Then** no "cannot find symbol" errors for method calls should occur

**Given** ID type mismatches exist  
**When** consistent ID types are used throughout  
**Then** no "incompatible types" errors should occur

**Given** repository methods are missing  
**When** all required repository methods are implemented  
**Then** no "cannot find symbol" errors for repository calls should occur

## Testing Requirements

**Verification Steps:**
1. Run `./gradlew compileJava` - should complete without errors
2. Run `./gradlew build -x test` - should compile successfully
3. Verify no compilation errors in build output
4. Check that all 39 compilation errors are resolved

**Success Criteria:**
- Zero compilation errors
- All Java files compile successfully
- Build completes without failures
- No "cannot find symbol" or "package does not exist" errors

## Error Reference

**Current error count:** 39 compilation errors

**Main error categories:**
1. `package javax.persistence does not exist` (8 errors)
2. `cannot find symbol` for missing methods (25 errors)  
3. `incompatible types` for ID mismatches (4 errors)
4. Missing JPA annotations (2 errors)

**Priority:** Fix in this order:
1. JPA imports (will resolve 8 errors immediately)
2. Missing entity methods (will resolve 25 errors)
3. ID type consistency (will resolve 4 errors)
4. Missing annotations (will resolve 2 errors)