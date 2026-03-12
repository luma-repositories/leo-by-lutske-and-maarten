# Create domain event testing framework

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Establish specialized testing framework for domain events, providing utilities to test event publishing, handling, and verification in isolation.

## Scope

- Create DomainEventTestBase class for event testing
- Implement event assertion utilities
- Provide patterns for testing event-driven behavior
- Create utilities for event verification

## Out of Scope

- Use case testing framework (separate task)
- Repository testing framework (separate task)
- Integration testing setup
- Event handler implementation testing

## Implementation Details

### DomainEventTestBase Class

Create `be.lutske.leolegacy.domain.test.event.DomainEventTestBase`:

```java
public abstract class DomainEventTestBase {
    
    protected MockDomainEventPublisher eventPublisher;
    
    @BeforeEach
    void setupEventTest() {
        eventPublisher = new MockDomainEventPublisher();
    }
    
    @AfterEach
    void cleanupEventTest() {
        eventPublisher.clear();
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
    
    protected <T extends DomainEvent> List<T> getPublishedEvents(Class<T> eventType) {
        return eventPublisher.getEventsOfType(eventType);
    }
    
    protected void assertEventCount(int expectedCount) {
        assertEquals(expectedCount, eventPublisher.getEventCount(), 
            "Expected " + expectedCount + " events to be published");
    }
    
    protected <T extends DomainEvent> void assertEventCountOfType(Class<T> eventType, int expectedCount) {
        int actualCount = eventPublisher.getEventCountOfType(eventType);
        assertEquals(expectedCount, actualCount, 
            "Expected " + expectedCount + " events of type " + eventType.getSimpleName() + 
            " but found " + actualCount);
    }
    
    protected void assertEventOrder(Class<? extends DomainEvent>... expectedEventTypes) {
        List<DomainEvent> events = eventPublisher.getPublishedEvents();
        assertEquals(expectedEventTypes.length, events.size(), 
            "Expected " + expectedEventTypes.length + " events but found " + events.size());
        
        for (int i = 0; i < expectedEventTypes.length; i++) {
            assertTrue(expectedEventTypes[i].isInstance(events.get(i)), 
                "Expected event at position " + i + " to be of type " + 
                expectedEventTypes[i].getSimpleName() + " but was " + 
                events.get(i).getClass().getSimpleName());
        }
    }
    
    protected <T extends DomainEvent> void assertEventContainsData(Class<T> eventType, 
                                                                   Consumer<T> eventAssertion) {
        T event = getPublishedEvent(eventType);
        eventAssertion.accept(event);
    }
    
    protected void publishEvent(DomainEvent event) {
        eventPublisher.publish(event);
    }
    
    protected void publishEvents(DomainEvent... events) {
        for (DomainEvent event : events) {
            eventPublisher.publish(event);
        }
    }
}
```

### Event Testing Utilities

Create `be.lutske.leolegacy.domain.test.event.EventTestUtils`:

```java
public final class EventTestUtils {
    
    private EventTestUtils() {
        // Utility class
    }
    
    public static RecipeCreatedEvent createRecipeCreatedEvent(RecipeId recipeId, String title) {
        return new RecipeCreatedEvent(
            UUID.randomUUID(),
            recipeId,
            title,
            LocalDateTime.now()
        );
    }
    
    public static RecipeUpdatedEvent createRecipeUpdatedEvent(RecipeId recipeId, String title) {
        return new RecipeUpdatedEvent(
            UUID.randomUUID(),
            recipeId,
            title,
            LocalDateTime.now()
        );
    }
    
    public static RecipeDeletedEvent createRecipeDeletedEvent(RecipeId recipeId) {
        return new RecipeDeletedEvent(
            UUID.randomUUID(),
            recipeId,
            LocalDateTime.now()
        );
    }
    
    public static CategoryCreatedEvent createCategoryCreatedEvent(CategoryId categoryId, String name) {
        return new CategoryCreatedEvent(
            UUID.randomUUID(),
            categoryId,
            name,
            LocalDateTime.now()
        );
    }
    
    public static CategoryUpdatedEvent createCategoryUpdatedEvent(CategoryId categoryId, String name) {
        return new CategoryUpdatedEvent(
            UUID.randomUUID(),
            categoryId,
            name,
            LocalDateTime.now()
        );
    }
    
    public static CategoryDeletedEvent createCategoryDeletedEvent(CategoryId categoryId) {
        return new CategoryDeletedEvent(
            UUID.randomUUID(),
            categoryId,
            LocalDateTime.now()
        );
    }
    
    public static void assertEventTimestamp(DomainEvent event, LocalDateTime expectedTime, Duration tolerance) {
        LocalDateTime eventTime = event.getOccurredAt();
        Duration difference = Duration.between(expectedTime, eventTime).abs();
        assertTrue(difference.compareTo(tolerance) <= 0, 
            "Event timestamp " + eventTime + " is not within " + tolerance + " of expected time " + expectedTime);
    }
    
    public static void assertEventId(DomainEvent event) {
        assertNotNull(event.getEventId(), "Event ID should not be null");
        assertNotEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), event.getEventId(), 
            "Event ID should not be empty UUID");
    }
}
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/test/event/DomainEventTestBase.java`
- `be/lutske/leolegacy/domain/test/event/EventTestUtils.java`

**Dependencies required:**
- MockDomainEventPublisher from application test framework
- Domain event classes (RecipeCreatedEvent, etc.)

**Directory structure:**
```
be/lutske/leolegacy/domain/test/event/
├── DomainEventTestBase.java
└── EventTestUtils.java
```

## Acceptance Criteria

**Given** domain event testing framework is implemented  
**When** event-related tests are written  
**Then** they should verify event publishing and handling behavior

**Given** DomainEventTestBase is implemented  
**When** domain event tests extend this base class  
**Then** they should have utilities for event verification and assertions

**Given** event assertion methods are implemented  
**When** events are published during testing  
**Then** they should be easily verifiable with clear assertion messages

**Given** EventTestUtils is implemented  
**When** test events need to be created  
**Then** they should be easily created with valid data

## Testing Requirements

**Framework Tests:**

Create `DomainEventTestBaseTest.java`:
- Test base class setup and cleanup functionality
- Test event assertion methods
- Test event counting and filtering
- Test event order verification
- Verify test isolation

Create `EventTestUtilsTest.java`:
- Test event creation utilities
- Test event assertion utilities
- Test timestamp verification
- Test event ID validation

**Test Coverage Requirements:**
- All event testing utilities must have unit tests
- Event assertion methods must be thoroughly tested
- Event creation utilities must be verified
- Test isolation must be confirmed