# Enhance testing architecture for domain layer

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Establish comprehensive testing architecture that supports the new clean architecture layers, ensuring domain logic can be tested in isolation and integration tests verify proper layer interactions.

## Scope

- Create testing utilities and base classes for domain layer testing
- Implement test doubles (mocks, fakes) for repository interfaces
- Create integration test framework for use case testing
- Establish testing patterns for domain events
- Create architectural testing to verify clean architecture compliance
- Implement test data builders for domain entities
- Ensure test coverage for all business rules and domain logic

## Out of Scope

- Performance testing framework
- End-to-end testing changes (maintain existing)
- Frontend testing changes
- Database migration testing

## Implementation Details

### Domain Layer Testing Framework

Create `be.lutske.leolegacy.domain.test.DomainTestBase`:

**Base class for domain entity tests:**
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

Create `be.lutske.leolegacy.domain.test.builder.RecipeTestDataBuilder`:

**Test data builder for Recipe entities:**
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

**Test data builder for Category entities:**
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

### Repository Test Doubles

Create `be.lutske.leolegacy.domain.test.repository.InMemoryRecipeRepository`:

**In-memory fake implementation for testing:**
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
    
    // Additional methods for test setup
    public void clear() {
        recipes.clear();
        recipesByCategory.clear();
    }
    
    public int size() {
        return recipes.size();
    }
}
```

Create `be.lutske.leolegacy.domain.test.repository.InMemoryCategoryRepository`:

**In-memory fake implementation for testing:**
```java
public class InMemoryCategoryRepository implements CategoryRepository {
    
    private final Map<CategoryId, Category> categories = new HashMap<>();
    private final Map<String, Category> categoriesByName = new HashMap<>();
    
    @Override
    public Optional<Category> findById(CategoryId id) {
        return Optional.ofNullable(categories.get(id));
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
    
    // Additional test methods
    public void clear() {
        categories.clear();
        categoriesByName.clear();
    }
}
```

### Use Case Testing Framework

Create `be.lutske.leolegacy.application.test.UseCaseTestBase`:

**Base class for use case tests:**
```java
public abstract class UseCaseTestBase {
    
    protected InMemoryRecipeRepository recipeRepository;
    protected InMemoryCategoryRepository categoryRepository;
    protected MockDomainEventPublisher eventPublisher;
    
    @BeforeEach
    void setupUseCaseTest() {
        recipeRepository = new InMemoryRecipeRepository();
        categoryRepository = new InMemoryCategoryRepository();
        eventPublisher = new MockDomainEventPublisher();
    }
    
    @AfterEach
    void cleanupUseCaseTest() {
        recipeRepository.clear();
        categoryRepository.clear();
        eventPublisher.clear();
    }
    
    protected Category givenCategoryExists(String name) {
        Category category = CategoryTestDataBuilder.aCategory()
            .withName(name)
            .build();
        return categoryRepository.save(category);
    }
    
    protected Recipe givenRecipeExists(String title) {
        Recipe recipe = RecipeTestDataBuilder.aRecipe()
            .withTitle(title)
            .build();
        return recipeRepository.save(recipe);
    }
}
```

Create `be.lutske.leolegacy.application.test.MockDomainEventPublisher`:

**Mock implementation for event publishing:**
```java
public class MockDomainEventPublisher implements DomainEventPublisher {
    
    private final List<DomainEvent> publishedEvents = new ArrayList<>();
    
    @Override
    public void publish(DomainEvent event) {
        publishedEvents.add(event);
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        publishedEvents.addAll(events);
    }
    
    public List<DomainEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }
    
    public <T extends DomainEvent> List<T> getEventsOfType(Class<T> eventType) {
        return publishedEvents.stream()
            .filter(eventType::isInstance)
            .map(eventType::cast)
            .collect(Collectors.toList());
    }
    
    public void clear() {
        publishedEvents.clear();
    }
    
    public boolean hasEventOfType(Class<? extends DomainEvent> eventType) {
        return publishedEvents.stream()
            .anyMatch(eventType::isInstance);
    }
}
```

### Domain Event Testing Framework

Create `be.lutske.leolegacy.domain.test.event.DomainEventTestBase`:

**Base class for domain event tests:**
```java
public abstract class DomainEventTestBase {
    
    protected MockDomainEventPublisher eventPublisher;
    
    @BeforeEach
    void setupEventTest() {
        eventPublisher = new MockDomainEventPublisher();
    }
    
    protected void assertEventPublished(Class<? extends DomainEvent> eventType) {
        assertTrue(eventPublisher.hasEventOfType(eventType), 
            "Expected event of type " + eventType.getSimpleName() + " to be published");
    }
    
    protected void assertNoEventsPublished() {
        assertTrue(eventPublisher.getPublishedEvents().isEmpty(), 
            "Expected no events to be published");
    }
    
    protected <T extends DomainEvent> T getPublishedEvent(Class<T> eventType) {
        List<T> events = eventPublisher.getEventsOfType(eventType);
        assertEquals(1, events.size(), 
            "Expected exactly one event of type " + eventType.getSimpleName());
        return events.get(0);
    }
}
```

### Architectural Testing

Create `be.lutske.leolegacy.architecture.test.CleanArchitectureTest`:

**Architectural compliance tests:**
```java
@AnalyzeClasses(packages = "be.lutske.leolegacy")
public class CleanArchitectureTest {
    
    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnInfrastructure = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..interfaceadapter..");
    
    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnApplication = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..");
    
    @ArchTest
    static final ArchRule applicationLayerShouldNotDependOnInfrastructure = 
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..interfaceadapter..");
    
    @ArchTest
    static final ArchRule interfaceAdaptersShouldNotDependOnInfrastructure = 
        noClasses()
            .that().resideInAPackage("..interfaceadapter..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");
    
    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces = 
        classes()
            .that().resideInAPackage("..domain.repository..")
            .should().beInterfaces();
    
    @ArchTest
    static final ArchRule useCasesShouldBeAnnotatedWithTransactional = 
        classes()
            .that().resideInAPackage("..application.usecase..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beAnnotatedWith(Transactional.class);
}
```

### Integration Testing Framework

Create `be.lutske.leolegacy.integration.test.IntegrationTestBase`:

**Base class for integration tests:**
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
    
    @BeforeEach
    void setupIntegrationTest() {
        // Clean database state
        cleanDatabase();
    }
    
    private void cleanDatabase() {
        // Clean test data
    }
    
    protected Category createTestCategory(String name) {
        // Create category through use case
    }
    
    protected Recipe createTestRecipe(String title, CategoryId categoryId) {
        // Create recipe through use case
    }
}
```

### Test Coverage and Quality

**Coverage Requirements:**

- Domain entities: 100% line coverage for business logic
- Use cases: 100% line coverage for all paths
- Repository interfaces: Contract tests for all methods
- Event handlers: 100% coverage for event processing
- Infrastructure adapters: 90% coverage for mapping logic

**Test Quality Standards:**

- All tests must be deterministic and repeatable
- Tests must not depend on external systems
- Test data must be isolated and cleaned up
- Tests must verify behavior, not implementation
- Integration tests must verify layer interactions

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/test/DomainTestBase.java`
- `be/lutske/leolegacy/domain/test/builder/RecipeTestDataBuilder.java`
- `be/lutske/leolegacy/domain/test/builder/CategoryTestDataBuilder.java`
- `be/lutske/leolegacy/domain/test/repository/InMemoryRecipeRepository.java`
- `be/lutske/leolegacy/domain/test/repository/InMemoryCategoryRepository.java`
- `be/lutske/leolegacy/domain/test/event/DomainEventTestBase.java`
- `be/lutske/leolegacy/application/test/UseCaseTestBase.java`
- `be/lutske/leolegacy/application/test/MockDomainEventPublisher.java`
- `be/lutske/leolegacy/integration/test/IntegrationTestBase.java`
- `be/lutske/leolegacy/architecture/test/CleanArchitectureTest.java`

**Test dependencies to add:**
- ArchUnit for architectural testing
- Testcontainers for database integration tests (if needed)
- Additional assertion libraries (AssertJ)

**Directory structure:**
```
be/lutske/leolegacy/
├── domain/test/
│   ├── DomainTestBase.java
│   ├── builder/
│   │   ├── RecipeTestDataBuilder.java
│   │   └── CategoryTestDataBuilder.java
│   ├── repository/
│   │   ├── InMemoryRecipeRepository.java
│   │   └── InMemoryCategoryRepository.java
│   └── event/
│       └── DomainEventTestBase.java
├── application/test/
│   ├── UseCaseTestBase.java
│   └── MockDomainEventPublisher.java
├── integration/test/
│   └── IntegrationTestBase.java
└── architecture/test/
    └── CleanArchitectureTest.java
```

## Acceptance Criteria

**Given** domain testing framework is implemented  
**When** domain entity tests are written  
**Then** they should be able to test business logic in isolation

**Given** repository test doubles are implemented  
**When** use case tests are written  
**Then** they should not require database connections

**Given** test data builders are implemented  
**When** test data is needed  
**Then** it should be easy to create valid domain entities with custom properties

**Given** architectural tests are implemented  
**When** they are executed  
**Then** they should verify clean architecture compliance

**Given** integration testing framework is implemented  
**When** integration tests are written  
**Then** they should verify proper layer interactions

**Given** domain event testing framework is implemented  
**When** event-related tests are written  
**Then** they should verify event publishing and handling behavior

**Given** all testing frameworks are implemented  
**When** the test suite is executed  
**Then** it should provide comprehensive coverage of business logic

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

Create `InMemoryRepositoryTest.java`:
- Test repository contract compliance
- Test data persistence and retrieval
- Test query operations

Create `MockDomainEventPublisherTest.java`:
- Test event publishing functionality
- Test event filtering and retrieval
- Test event clearing

**Architectural Tests:**

Create comprehensive architectural tests:
- Verify layer dependencies
- Test package structure compliance
- Verify annotation usage
- Test interface vs implementation patterns

**Integration Tests:**

Create `TestingFrameworkIntegrationTest.java`:
- Test framework integration with Quarkus
- Test dependency injection in test context
- Verify transaction behavior in tests
- Test database cleanup functionality

**Test Coverage Requirements:**
- All testing utilities must have unit tests
- Architectural tests must cover all layers
- Integration tests must verify framework functionality
- Test data builders must be thoroughly tested