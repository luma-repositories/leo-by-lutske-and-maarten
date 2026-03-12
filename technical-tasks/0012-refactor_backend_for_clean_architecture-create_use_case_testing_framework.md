# Create use case testing framework

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Establish testing framework for application layer use cases, providing base classes and utilities for testing business logic with mocked dependencies.

## Scope

- Create UseCaseTestBase class for use case testing
- Implement MockDomainEventPublisher for event testing
- Provide utilities for test data setup
- Establish patterns for use case testing

## Out of Scope

- Domain testing framework (separate task)
- Repository test doubles (separate task)
- Integration testing setup
- Architectural testing

## Implementation Details

### UseCaseTestBase Class

Create `be.lutske.leolegacy.application.test.UseCaseTestBase`:

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
    
    protected Recipe givenRecipeExistsInCategory(String title, CategoryId categoryId) {
        Recipe recipe = RecipeTestDataBuilder.aRecipe()
            .withTitle(title)
            .withCategory(categoryId)
            .build();
        return recipeRepository.save(recipe);
    }
    
    protected void assertRecipeExists(RecipeId recipeId) {
        assertTrue(recipeRepository.existsById(recipeId), 
            "Expected recipe with ID " + recipeId + " to exist");
    }
    
    protected void assertRecipeDoesNotExist(RecipeId recipeId) {
        assertFalse(recipeRepository.existsById(recipeId), 
            "Expected recipe with ID " + recipeId + " to not exist");
    }
    
    protected void assertCategoryExists(CategoryId categoryId) {
        assertTrue(categoryRepository.existsById(categoryId), 
            "Expected category with ID " + categoryId + " to exist");
    }
    
    protected void assertRepositorySize(int expectedRecipes, int expectedCategories) {
        assertEquals(expectedRecipes, recipeRepository.size(), 
            "Expected " + expectedRecipes + " recipes in repository");
        assertEquals(expectedCategories, categoryRepository.size(), 
            "Expected " + expectedCategories + " categories in repository");
    }
}
```

### MockDomainEventPublisher

Create `be.lutske.leolegacy.application.test.MockDomainEventPublisher`:

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
    
    public int getEventCount() {
        return publishedEvents.size();
    }
    
    public <T extends DomainEvent> int getEventCountOfType(Class<T> eventType) {
        return getEventsOfType(eventType).size();
    }
    
    public void assertEventPublished(Class<? extends DomainEvent> eventType) {
        assertTrue(hasEventOfType(eventType), 
            "Expected event of type " + eventType.getSimpleName() + " to be published");
    }
    
    public void assertNoEventsPublished() {
        assertTrue(publishedEvents.isEmpty(), 
            "Expected no events to be published");
    }
    
    public <T extends DomainEvent> T getPublishedEvent(Class<T> eventType) {
        List<T> events = getEventsOfType(eventType);
        assertEquals(1, events.size(), 
            "Expected exactly one event of type " + eventType.getSimpleName());
        return events.get(0);
    }
    
    public <T extends DomainEvent> void assertEventPublishedWithCount(Class<T> eventType, int expectedCount) {
        int actualCount = getEventCountOfType(eventType);
        assertEquals(expectedCount, actualCount, 
            "Expected " + expectedCount + " events of type " + eventType.getSimpleName() + 
            " but found " + actualCount);
    }
}
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/application/test/UseCaseTestBase.java`
- `be/lutske/leolegacy/application/test/MockDomainEventPublisher.java`

**Dependencies required:**
- Domain test framework (RecipeTestDataBuilder, CategoryTestDataBuilder)
- Repository test doubles (InMemoryRecipeRepository, InMemoryCategoryRepository)

**Directory structure:**
```
be/lutske/leolegacy/application/test/
├── UseCaseTestBase.java
└── MockDomainEventPublisher.java
```

## Acceptance Criteria

**Given** use case testing framework is implemented  
**When** use case tests are written  
**Then** they should have access to repository test doubles and event publisher

**Given** MockDomainEventPublisher is implemented  
**When** domain events are published during use case execution  
**Then** they should be captured and available for verification

**Given** UseCaseTestBase is implemented  
**When** use case tests extend this base class  
**Then** they should have utilities for test data setup and assertions

**Given** test setup and cleanup methods are implemented  
**When** use case tests are executed  
**Then** they should start with clean state and clean up after execution

## Testing Requirements

**Framework Tests:**

Create `UseCaseTestBaseTest.java`:
- Test base class setup and cleanup functionality
- Test test data creation utilities
- Test assertion methods
- Verify test isolation

Create `MockDomainEventPublisherTest.java`:
- Test event publishing functionality
- Test event filtering and retrieval
- Test event counting methods
- Test assertion methods
- Test event clearing

**Test Coverage Requirements:**
- All testing utilities must have unit tests
- Event publisher functionality must be thoroughly tested
- Base class utilities must be verified
- Test isolation must be confirmed