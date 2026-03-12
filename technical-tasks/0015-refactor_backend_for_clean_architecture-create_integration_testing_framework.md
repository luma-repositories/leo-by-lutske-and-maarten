# Create integration testing framework

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Establish integration testing framework that verifies proper layer interactions and end-to-end functionality within the clean architecture, using real dependencies where appropriate.

## Scope

- Create IntegrationTestBase class for integration testing
- Implement database integration testing setup
- Create utilities for integration test data management
- Establish patterns for testing layer interactions

## Out of Scope

- Unit testing frameworks (separate tasks)
- Architectural testing (separate task)
- End-to-end API testing
- Performance testing

## Implementation Details

### IntegrationTestBase Class

Create `be.lutske.leolegacy.integration.test.IntegrationTestBase`:

```java
@QuarkusTest
@TestTransaction
public abstract class IntegrationTestBase {
    
    @Inject
    protected RecipeRepository recipeRepository;
    
    @Inject
    protected CategoryRepository categoryRepository;
    
    @Inject
    protected CreateRecipeUseCase createRecipeUseCase;
    
    @Inject
    protected UpdateRecipeUseCase updateRecipeUseCase;
    
    @Inject
    protected DeleteRecipeUseCase deleteRecipeUseCase;
    
    @Inject
    protected FindRecipeUseCase findRecipeUseCase;
    
    @Inject
    protected CreateCategoryUseCase createCategoryUseCase;
    
    @Inject
    protected UpdateCategoryUseCase updateCategoryUseCase;
    
    @Inject
    protected DeleteCategoryUseCase deleteCategoryUseCase;
    
    @Inject
    protected FindCategoryUseCase findCategoryUseCase;
    
    @BeforeEach
    void setupIntegrationTest() {
        cleanDatabase();
    }
    
    @AfterEach
    void cleanupIntegrationTest() {
        cleanDatabase();
    }
    
    private void cleanDatabase() {
        // Clean test data in proper order (recipes first, then categories)
        recipeRepository.findAll().forEach(recipe -> 
            recipeRepository.deleteById(recipe.getId()));
        categoryRepository.findAll().forEach(category -> 
            categoryRepository.deleteById(category.getId()));
    }
    
    protected Category createTestCategory(String name) {
        CreateCategoryCommand command = new CreateCategoryCommand(name, "Test description for " + name);
        return createCategoryUseCase.execute(command);
    }
    
    protected Category createTestCategory(String name, String description) {
        CreateCategoryCommand command = new CreateCategoryCommand(name, description);
        return createCategoryUseCase.execute(command);
    }
    
    protected Recipe createTestRecipe(String title) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Test Ingredient", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected Recipe createTestRecipe(String title, CategoryId categoryId) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .categoryId(categoryId)
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Test Ingredient", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected Recipe createTestRecipeWithIngredients(String title, List<CreateRecipeIngredientCommand> ingredients) {
        CreateRecipeCommand command = CreateRecipeCommand.builder()
            .title(title)
            .description("Test description for " + title)
            .ingredients(ingredients)
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Test instruction")
            ))
            .build();
        return createRecipeUseCase.execute(command);
    }
    
    protected void assertRecipeExistsInDatabase(RecipeId recipeId) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        assertTrue(recipe.isPresent(), "Expected recipe with ID " + recipeId + " to exist in database");
    }
    
    protected void assertRecipeDoesNotExistInDatabase(RecipeId recipeId) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        assertFalse(recipe.isPresent(), "Expected recipe with ID " + recipeId + " to not exist in database");
    }
    
    protected void assertCategoryExistsInDatabase(CategoryId categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        assertTrue(category.isPresent(), "Expected category with ID " + categoryId + " to exist in database");
    }
    
    protected void assertCategoryDoesNotExistInDatabase(CategoryId categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        assertFalse(category.isPresent(), "Expected category with ID " + categoryId + " to not exist in database");
    }
    
    protected void assertDatabaseCounts(int expectedRecipes, int expectedCategories) {
        List<Recipe> recipes = recipeRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        
        assertEquals(expectedRecipes, recipes.size(), 
            "Expected " + expectedRecipes + " recipes in database but found " + recipes.size());
        assertEquals(expectedCategories, categories.size(), 
            "Expected " + expectedCategories + " categories in database but found " + categories.size());
    }
}
```

### Integration Test Data Builders

Create `be.lutske.leolegacy.integration.test.builder.IntegrationTestDataBuilder`:

```java
public class IntegrationTestDataBuilder {
    
    public static CreateRecipeCommand.Builder aCreateRecipeCommand() {
        return CreateRecipeCommand.builder()
            .title("Integration Test Recipe")
            .description("Recipe created for integration testing")
            .ingredients(List.of(
                new CreateRecipeIngredientCommand("Flour", "2 cups", "cups"),
                new CreateRecipeIngredientCommand("Sugar", "1 cup", "cup")
            ))
            .instructions(List.of(
                new CreateRecipeInstructionCommand(1, "Mix dry ingredients"),
                new CreateRecipeInstructionCommand(2, "Add wet ingredients"),
                new CreateRecipeInstructionCommand(3, "Bake for 30 minutes")
            ));
    }
    
    public static CreateCategoryCommand aCreateCategoryCommand() {
        return new CreateCategoryCommand(
            "Integration Test Category",
            "Category created for integration testing"
        );
    }
    
    public static CreateCategoryCommand aCreateCategoryCommand(String name) {
        return new CreateCategoryCommand(
            name,
            "Category created for integration testing: " + name
        );
    }
    
    public static UpdateRecipeCommand.Builder anUpdateRecipeCommand(RecipeId recipeId) {
        return UpdateRecipeCommand.builder()
            .recipeId(recipeId)
            .title("Updated Integration Test Recipe")
            .description("Updated recipe for integration testing")
            .ingredients(List.of(
                new UpdateRecipeIngredientCommand("Updated Flour", "3 cups", "cups"),
                new UpdateRecipeIngredientCommand("Updated Sugar", "1.5 cups", "cups")
            ))
            .instructions(List.of(
                new UpdateRecipeInstructionCommand(1, "Mix updated dry ingredients"),
                new UpdateRecipeInstructionCommand(2, "Add updated wet ingredients"),
                new UpdateRecipeInstructionCommand(3, "Bake for 35 minutes")
            ));
    }
    
    public static UpdateCategoryCommand anUpdateCategoryCommand(CategoryId categoryId) {
        return new UpdateCategoryCommand(
            categoryId,
            "Updated Integration Test Category",
            "Updated category for integration testing"
        );
    }
    
    public static List<CreateRecipeIngredientCommand> standardIngredients() {
        return List.of(
            new CreateRecipeIngredientCommand("Flour", "2 cups", "cups"),
            new CreateRecipeIngredientCommand("Sugar", "1 cup", "cup"),
            new CreateRecipeIngredientCommand("Eggs", "2 pieces", "pieces"),
            new CreateRecipeIngredientCommand("Milk", "1 cup", "cup")
        );
    }
    
    public static List<CreateRecipeInstructionCommand> standardInstructions() {
        return List.of(
            new CreateRecipeInstructionCommand(1, "Preheat oven to 350°F"),
            new CreateRecipeInstructionCommand(2, "Mix dry ingredients in a bowl"),
            new CreateRecipeInstructionCommand(3, "In separate bowl, beat eggs and add milk"),
            new CreateRecipeInstructionCommand(4, "Combine wet and dry ingredients"),
            new CreateRecipeInstructionCommand(5, "Pour into greased pan"),
            new CreateRecipeInstructionCommand(6, "Bake for 30-35 minutes")
        );
    }
}
```

### Database Integration Test Configuration

Create `be.lutske.leolegacy.integration.test.config.TestDatabaseConfig`:

```java
@TestProfile("integration-test")
@ApplicationScoped
public class TestDatabaseConfig {
    
    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;
    
    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;
    
    @PostConstruct
    void logTestConfiguration() {
        System.out.println("Integration test database configuration:");
        System.out.println("  DB Kind: " + dbKind);
        System.out.println("  JDBC URL: " + jdbcUrl);
    }
    
    @PreDestroy
    void cleanupTestConfiguration() {
        System.out.println("Cleaning up integration test database configuration");
    }
}
```

### Test Profile Configuration

Create `application-integration-test.properties`:

```properties
# Integration test database configuration
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:integration-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
quarkus.datasource.username=sa
quarkus.datasource.password=

# Hibernate configuration for tests
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=no-file

# Logging configuration for tests
quarkus.log.level=INFO
quarkus.log.category."be.lutske.leolegacy".level=DEBUG

# Transaction configuration
quarkus.transaction-manager.enable-recovery=false
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/integration/test/IntegrationTestBase.java`
- `be/lutske/leolegacy/integration/test/builder/IntegrationTestDataBuilder.java`
- `be/lutske/leolegacy/integration/test/config/TestDatabaseConfig.java`
- `src/test/resources/application-integration-test.properties`

**Dependencies required:**
- Use case implementations
- Repository implementations
- Quarkus test framework

**Directory structure:**
```
be/lutske/leolegacy/integration/test/
├── IntegrationTestBase.java
├── builder/
│   └── IntegrationTestDataBuilder.java
└── config/
    └── TestDatabaseConfig.java
```

## Acceptance Criteria

**Given** integration testing framework is implemented  
**When** integration tests are written  
**Then** they should verify proper layer interactions

**Given** IntegrationTestBase is implemented  
**When** integration tests extend this base class  
**Then** they should have access to real use cases and repositories

**Given** database integration setup is implemented  
**When** integration tests are executed  
**Then** they should use a test database with proper cleanup

**Given** test data builders are implemented  
**When** integration test data is needed  
**Then** it should be easy to create valid commands and entities

## Testing Requirements

**Framework Tests:**

Create `IntegrationTestBaseTest.java`:
- Test base class setup and cleanup functionality
- Test database cleaning mechanisms
- Test test data creation utilities
- Verify dependency injection works correctly

Create `IntegrationTestDataBuilderTest.java`:
- Test command builder functionality
- Test all builder methods
- Verify created commands are valid

Create `TestDatabaseConfigTest.java`:
- Test database configuration loading
- Test test profile activation
- Verify database connection properties

**Integration Test Examples:**

Create `RecipeUseCaseIntegrationTest.java`:
- Test complete recipe lifecycle (create, read, update, delete)
- Test recipe-category relationships
- Test transaction behavior
- Test error handling

Create `CategoryUseCaseIntegrationTest.java`:
- Test complete category lifecycle
- Test category-recipe relationships
- Test constraint violations

**Test Coverage Requirements:**
- All integration testing utilities must have unit tests
- Framework functionality must be thoroughly tested
- Database integration must be verified
- Transaction behavior must be tested