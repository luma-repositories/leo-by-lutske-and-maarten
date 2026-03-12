# Create domain testing framework

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Create base testing utilities and classes specifically for domain layer testing, enabling isolated testing of domain entities and business logic.

## Scope

- Create DomainTestBase class with common test setup
- Implement test data builders for domain entities
- Provide utilities for creating valid test data
- Establish patterns for domain entity testing

## Out of Scope

- Repository testing framework
- Use case testing framework
- Integration testing setup
- Event testing framework

## Implementation Details

### DomainTestBase Class

Create `be.lutske.leolegacy.domain.test.DomainTestBase`:

```java
public abstract class DomainTestBase {
    
    protected static final UUID SAMPLE_RECIPE_ID = UUID.randomUUID();
    protected static final UUID SAMPLE_CATEGORY_ID = UUID.randomUUID();
    protected static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 3, 12, 10, 0);
    
    @BeforeEach
    void setupDomainTest() {
        // Common setup for domain tests
    }
    
    protected Recipe createValidRecipe() {
        return RecipeTestDataBuilder.aRecipe()
            .withId(RecipeId.from(SAMPLE_RECIPE_ID))
            .withTitle("Test Recipe")
            .withIngredients(List.of(createValidIngredient()))
            .withInstructions(List.of(createValidInstruction()))
            .build();
    }
    
    protected RecipeIngredient createValidIngredient() {
        return new RecipeIngredient("Flour", "2 cups", "cups");
    }
    
    protected RecipeInstruction createValidInstruction() {
        return new RecipeInstruction(1, "Mix ingredients together");
    }
}
```

### Test Data Builders

Create `be.lutske.leolegacy.domain.test.builder.RecipeTestDataBuilder`:

```java
public class RecipeTestDataBuilder {
    private RecipeId id = RecipeId.generate();
    private String title = "Default Recipe";
    private String description = "Default description";
    private List<RecipeIngredient> ingredients = List.of(defaultIngredient());
    private List<RecipeInstruction> instructions = List.of(defaultInstruction());
    private CategoryId categoryId = null;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public static RecipeTestDataBuilder aRecipe() {
        return new RecipeTestDataBuilder();
    }
    
    public RecipeTestDataBuilder withId(RecipeId id) {
        this.id = id;
        return this;
    }
    
    public RecipeTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public RecipeTestDataBuilder withIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
        return this;
    }
    
    public RecipeTestDataBuilder withInstructions(List<RecipeInstruction> instructions) {
        this.instructions = instructions;
        return this;
    }
    
    public RecipeTestDataBuilder withCategory(CategoryId categoryId) {
        this.categoryId = categoryId;
        return this;
    }
    
    public Recipe build() {
        return new Recipe(id, title, description, ingredients, instructions, categoryId, createdAt, updatedAt);
    }
}
```

Create `be.lutske.leolegacy.domain.test.builder.CategoryTestDataBuilder`:

```java
public class CategoryTestDataBuilder {
    private CategoryId id = CategoryId.generate();
    private String name = "Default Category";
    private String description = "Default description";
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public static CategoryTestDataBuilder aCategory() {
        return new CategoryTestDataBuilder();
    }
    
    public CategoryTestDataBuilder withId(CategoryId id) {
        this.id = id;
        return this;
    }
    
    public CategoryTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public Category build() {
        return new Category(id, name, description, createdAt);
    }
}
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/test/DomainTestBase.java`
- `be/lutske/leolegacy/domain/test/builder/RecipeTestDataBuilder.java`
- `be/lutske/leolegacy/domain/test/builder/CategoryTestDataBuilder.java`

**Directory structure:**
```
be/lutske/leolegacy/domain/test/
├── DomainTestBase.java
└── builder/
    ├── RecipeTestDataBuilder.java
    └── CategoryTestDataBuilder.java
```

## Acceptance Criteria

**Given** domain testing framework is implemented  
**When** domain entity tests are written  
**Then** they should be able to test business logic in isolation

**Given** test data builders are implemented  
**When** test data is needed  
**Then** it should be easy to create valid domain entities with custom properties

**Given** DomainTestBase is implemented  
**When** domain tests extend this base class  
**Then** they should have access to common test utilities and setup

## Testing Requirements

**Framework Tests:**

Create `DomainTestBaseTest.java`:
- Test base class functionality
- Test common test data creation
- Verify test isolation

Create `RecipeTestDataBuilderTest.java`:
- Test builder pattern functionality
- Test all builder methods
- Verify created entities are valid

Create `CategoryTestDataBuilderTest.java`:
- Test builder pattern functionality
- Test all builder methods
- Verify created entities are valid

**Test Coverage Requirements:**
- All testing utilities must have unit tests
- Test data builders must be thoroughly tested
- Base class functionality must be verified