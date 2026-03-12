# Create repository test doubles

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Implement in-memory fake implementations of repository interfaces to enable isolated testing of use cases without requiring database connections.

## Scope

- Create InMemoryRecipeRepository fake implementation
- Create InMemoryCategoryRepository fake implementation
- Implement all repository contract methods
- Provide test-specific utilities for setup and verification

## Out of Scope

- Domain testing framework (separate task)
- Use case testing framework (separate task)
- Integration testing setup
- Event testing framework

## Implementation Details

### InMemoryRecipeRepository

Create `be.lutske.leolegacy.domain.test.repository.InMemoryRecipeRepository`:

```java
public class InMemoryRecipeRepository implements RecipeRepository {
    
    private final Map<RecipeId, Recipe> recipes = new HashMap<>();
    private final Map<CategoryId, List<Recipe>> recipesByCategory = new HashMap<>();
    
    @Override
    public Optional<Recipe> findById(RecipeId id) {
        return Optional.ofNullable(recipes.get(id));
    }
    
    @Override
    public List<Recipe> findAll() {
        return new ArrayList<>(recipes.values());
    }
    
    @Override
    public List<Recipe> findByCategory(CategoryId categoryId) {
        return recipesByCategory.getOrDefault(categoryId, new ArrayList<>());
    }
    
    @Override
    public Recipe save(Recipe recipe) {
        recipes.put(recipe.getId(), recipe);
        updateCategoryIndex(recipe);
        return recipe;
    }
    
    @Override
    public void deleteById(RecipeId id) {
        Recipe recipe = recipes.remove(id);
        if (recipe != null) {
            removeCategoryIndex(recipe);
        }
    }
    
    @Override
    public boolean existsById(RecipeId id) {
        return recipes.containsKey(id);
    }
    
    // Test-specific methods
    public void clear() {
        recipes.clear();
        recipesByCategory.clear();
    }
    
    public int size() {
        return recipes.size();
    }
    
    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes.values());
    }
    
    private void updateCategoryIndex(Recipe recipe) {
        if (recipe.getCategoryId() != null) {
            recipesByCategory.computeIfAbsent(recipe.getCategoryId(), k -> new ArrayList<>())
                .add(recipe);
        }
    }
    
    private void removeCategoryIndex(Recipe recipe) {
        if (recipe.getCategoryId() != null) {
            List<Recipe> categoryRecipes = recipesByCategory.get(recipe.getCategoryId());
            if (categoryRecipes != null) {
                categoryRecipes.removeIf(r -> r.getId().equals(recipe.getId()));
                if (categoryRecipes.isEmpty()) {
                    recipesByCategory.remove(recipe.getCategoryId());
                }
            }
        }
    }
}
```

### InMemoryCategoryRepository

Create `be.lutske.leolegacy.domain.test.repository.InMemoryCategoryRepository`:

```java
public class InMemoryCategoryRepository implements CategoryRepository {
    
    private final Map<CategoryId, Category> categories = new HashMap<>();
    private final Map<String, Category> categoriesByName = new HashMap<>();
    
    @Override
    public Optional<Category> findById(CategoryId id) {
        return Optional.ofNullable(categories.get(id));
    }
    
    @Override
    public Optional<Category> findByName(String name) {
        return Optional.ofNullable(categoriesByName.get(name));
    }
    
    @Override
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }
    
    @Override
    public Category save(Category category) {
        categories.put(category.getId(), category);
        categoriesByName.put(category.getName(), category);
        return category;
    }
    
    @Override
    public void deleteById(CategoryId id) {
        Category category = categories.remove(id);
        if (category != null) {
            categoriesByName.remove(category.getName());
        }
    }
    
    @Override
    public boolean existsById(CategoryId id) {
        return categories.containsKey(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return categoriesByName.containsKey(name);
    }
    
    // Test-specific methods
    public void clear() {
        categories.clear();
        categoriesByName.clear();
    }
    
    public int size() {
        return categories.size();
    }
    
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }
}
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/test/repository/InMemoryRecipeRepository.java`
- `be/lutske/leolegacy/domain/test/repository/InMemoryCategoryRepository.java`

**Directory structure:**
```
be/lutske/leolegacy/domain/test/repository/
├── InMemoryRecipeRepository.java
└── InMemoryCategoryRepository.java
```

## Acceptance Criteria

**Given** repository test doubles are implemented  
**When** use case tests are written  
**Then** they should not require database connections

**Given** InMemoryRecipeRepository is implemented  
**When** recipe operations are performed  
**Then** they should behave consistently with the repository contract

**Given** InMemoryCategoryRepository is implemented  
**When** category operations are performed  
**Then** they should behave consistently with the repository contract

**Given** test-specific methods are implemented  
**When** test setup and cleanup is needed  
**Then** repositories should provide clear() and size() methods

## Testing Requirements

**Repository Contract Tests:**

Create `InMemoryRecipeRepositoryTest.java`:
- Test repository contract compliance
- Test data persistence and retrieval
- Test query operations
- Test category indexing functionality
- Test clear and size methods

Create `InMemoryCategoryRepositoryTest.java`:
- Test repository contract compliance
- Test data persistence and retrieval
- Test name-based queries
- Test clear and size methods

**Test Coverage Requirements:**
- All repository methods must be tested
- Edge cases (null values, empty collections) must be covered
- Test-specific utility methods must be verified