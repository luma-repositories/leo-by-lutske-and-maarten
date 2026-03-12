# Implement domain events for recipe import workflow

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Implement domain events to decouple business operations and enable extensible workflow handling, particularly for the recipe import process where multiple concerns need to be notified of business state changes.

## Scope

- Create domain event infrastructure (event base class, publisher, handlers)
- Define recipe-related domain events (created, updated, imported)
- Implement event publishing from domain entities and use cases
- Create event handlers for cross-cutting concerns
- Ensure events are published within transaction boundaries
- Enable future extensibility for audit logging, notifications, etc.

## Out of Scope

- External event publishing (message queues, webhooks)
- Event sourcing implementation
- Event store persistence
- Asynchronous event processing (keep synchronous for now)
- UI notification implementation

## Implementation Details

### Domain Event Infrastructure

Create `be.lutske.leolegacy.domain.event.DomainEvent` (base interface):

**Properties:**
- `UUID eventId` - unique identifier for the event
- `LocalDateTime occurredAt` - when the event occurred
- `String eventType` - type identifier for the event
- `UUID aggregateId` - ID of the aggregate that generated the event

**Methods:**
- `UUID getEventId()`
- `LocalDateTime getOccurredAt()`
- `String getEventType()`
- `UUID getAggregateId()`

Create `be.lutske.leolegacy.domain.event.DomainEventPublisher`:

**Interface:**
- `void publish(DomainEvent event)` - publish single event
- `void publishAll(List<DomainEvent> events)` - publish multiple events

**Implementation Strategy:**
- Use CDI event system for in-process event handling
- Ensure events are published within transaction boundaries
- Support both immediate and deferred publishing

Create `be.lutske.leolegacy.domain.event.DomainEventHandler<T extends DomainEvent>`:

**Interface:**
- `void handle(T event)` - handle specific event type
- `Class<T> getEventType()` - return handled event type

### Recipe Domain Events

Create `be.lutske.leolegacy.domain.event.recipe.RecipeCreatedEvent`:

**Properties:**
- `RecipeId recipeId` - ID of created recipe
- `String title` - recipe title
- `CategoryId categoryId` - assigned category (optional)
- `LocalDateTime createdAt` - creation timestamp

**Usage:** Published when a new recipe is successfully created

Create `be.lutske.leolegacy.domain.event.recipe.RecipeUpdatedEvent`:

**Properties:**
- `RecipeId recipeId` - ID of updated recipe
- `String previousTitle` - title before update
- `String newTitle` - title after update
- `CategoryId previousCategoryId` - category before update (optional)
- `CategoryId newCategoryId` - category after update (optional)
- `LocalDateTime updatedAt` - update timestamp

**Usage:** Published when an existing recipe is successfully updated

Create `be.lutske.leolegacy.domain.event.recipe.RecipeImportedEvent`:

**Properties:**
- `RecipeId recipeId` - ID of imported recipe
- `String title` - imported recipe title
- `String extractionSource` - AI provider used for extraction
- `CategoryId categoryId` - assigned category (optional)
- `LocalDateTime importedAt` - import timestamp
- `int ingredientCount` - number of ingredients extracted
- `int instructionCount` - number of instructions extracted

**Usage:** Published when a recipe is successfully imported from AI extraction

Create `be.lutske.leolegacy.domain.event.recipe.RecipeDeletedEvent`:

**Properties:**
- `RecipeId recipeId` - ID of deleted recipe
- `String title` - title of deleted recipe
- `LocalDateTime deletedAt` - deletion timestamp

**Usage:** Published when a recipe is successfully deleted

### Event Publishing Integration

**Domain Entity Integration:**

Modify Recipe domain entity to support event generation:

**Add to Recipe class:**
- `private List<DomainEvent> domainEvents = new ArrayList<>()`
- `public List<DomainEvent> getDomainEvents()`
- `public void clearDomainEvents()`
- `private void addDomainEvent(DomainEvent event)`

**Event Generation Methods:**
- `markAsCreated()` - generates RecipeCreatedEvent
- `markAsUpdated(String previousTitle, CategoryId previousCategoryId)` - generates RecipeUpdatedEvent
- `markAsImported(String extractionSource, int ingredientCount, int instructionCount)` - generates RecipeImportedEvent

**Use Case Integration:**

Modify use cases to publish events after successful operations:

**CreateRecipeUseCase:**
```java
Recipe recipe = new Recipe(...);
recipe.markAsCreated();
Recipe savedRecipe = recipeRepository.save(recipe);
domainEventPublisher.publishAll(savedRecipe.getDomainEvents());
savedRecipe.clearDomainEvents();
```

**UpdateRecipeUseCase:**
```java
Recipe existingRecipe = recipeRepository.findById(id);
String previousTitle = existingRecipe.getTitle();
CategoryId previousCategoryId = existingRecipe.getCategoryId();
existingRecipe.updateTitle(newTitle);
existingRecipe.markAsUpdated(previousTitle, previousCategoryId);
Recipe savedRecipe = recipeRepository.save(existingRecipe);
domainEventPublisher.publishAll(savedRecipe.getDomainEvents());
savedRecipe.clearDomainEvents();
```

**ImportRecipeFromImageUseCase:**
```java
Recipe recipe = createRecipeFromExtraction(extractionResult);
recipe.markAsImported(extractionSource, ingredientCount, instructionCount);
Recipe savedRecipe = recipeRepository.save(recipe);
domainEventPublisher.publishAll(savedRecipe.getDomainEvents());
savedRecipe.clearDomainEvents();
```

### Event Handler Implementation

Create `be.lutske.leolegacy.application.event.RecipeAuditEventHandler`:

**Purpose:** Log recipe operations for audit trail

**Handles:**
- `RecipeCreatedEvent` - log recipe creation
- `RecipeUpdatedEvent` - log recipe updates with change details
- `RecipeImportedEvent` - log recipe imports with extraction details
- `RecipeDeletedEvent` - log recipe deletions

**Implementation:**
```java
@ApplicationScoped
public class RecipeAuditEventHandler {
    
    @Inject
    Logger logger;
    
    public void handleRecipeCreated(@Observes RecipeCreatedEvent event) {
        logger.info("Recipe created: {} (ID: {})", event.getTitle(), event.getRecipeId());
    }
    
    public void handleRecipeImported(@Observes RecipeImportedEvent event) {
        logger.info("Recipe imported: {} (ID: {}, Source: {}, Ingredients: {}, Instructions: {})", 
            event.getTitle(), event.getRecipeId(), event.getExtractionSource(), 
            event.getIngredientCount(), event.getInstructionCount());
    }
}
```

Create `be.lutske.leolegacy.application.event.RecipeStatisticsEventHandler`:

**Purpose:** Update recipe statistics and metrics

**Handles:**
- `RecipeCreatedEvent` - increment recipe count
- `RecipeImportedEvent` - increment import count, track AI usage
- `RecipeDeletedEvent` - decrement recipe count

### Event Publisher Implementation

Create `be.lutske.leolegacy.infrastructure.event.CdiDomainEventPublisher`:

**Implementation:**
```java
@ApplicationScoped
public class CdiDomainEventPublisher implements DomainEventPublisher {
    
    @Inject
    Event<DomainEvent> eventBroadcaster;
    
    @Override
    public void publish(DomainEvent event) {
        eventBroadcaster.fire(event);
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
```

### Transaction Integration

**Event Publishing Strategy:**

Events should be published within the same transaction as the business operation:

1. Business operation executes
2. Domain events are generated and stored in aggregate
3. Repository save operation completes
4. Events are published before transaction commit
5. Event handlers execute within the same transaction
6. Transaction commits if all operations succeed

**Rollback Behavior:**

If any event handler fails:
- Transaction should roll back
- Business operation should be undone
- No events should be processed
- Appropriate exception should be thrown

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/domain/event/DomainEvent.java`
- `be/lutske/leolegacy/domain/event/DomainEventPublisher.java`
- `be/lutske/leolegacy/domain/event/DomainEventHandler.java`
- `be/lutske/leolegacy/domain/event/recipe/RecipeCreatedEvent.java`
- `be/lutske/leolegacy/domain/event/recipe/RecipeUpdatedEvent.java`
- `be/lutske/leolegacy/domain/event/recipe/RecipeImportedEvent.java`
- `be/lutske/leolegacy/domain/event/recipe/RecipeDeletedEvent.java`
- `be/lutske/leolegacy/application/event/RecipeAuditEventHandler.java`
- `be/lutske/leolegacy/application/event/RecipeStatisticsEventHandler.java`
- `be/lutske/leolegacy/infrastructure/event/CdiDomainEventPublisher.java`

**Files to modify:**
- `be/lutske/leolegacy/domain/model/Recipe.java` (add event generation)
- `be/lutske/leolegacy/application/usecase/CreateRecipeUseCase.java` (add event publishing)
- `be/lutske/leolegacy/application/usecase/UpdateRecipeUseCase.java` (add event publishing)
- `be/lutske/leolegacy/application/usecase/ImportRecipeFromImageUseCase.java` (add event publishing)
- `be/lutske/leolegacy/application/usecase/DeleteRecipeUseCase.java` (add event publishing)

**Directory structure:**
```
be/lutske/leolegacy/
├── domain/
│   ├── event/
│   │   ├── DomainEvent.java
│   │   ├── DomainEventPublisher.java
│   │   ├── DomainEventHandler.java
│   │   └── recipe/
│   │       ├── RecipeCreatedEvent.java
│   │       ├── RecipeUpdatedEvent.java
│   │       ├── RecipeImportedEvent.java
│   │       └── RecipeDeletedEvent.java
│   └── model/
│       └── Recipe.java (modified)
├── application/
│   ├── event/
│   │   ├── RecipeAuditEventHandler.java
│   │   └── RecipeStatisticsEventHandler.java
│   └── usecase/
│       └── (modified use cases)
└── infrastructure/
    └── event/
        └── CdiDomainEventPublisher.java
```

## Acceptance Criteria

**Given** a recipe is created through CreateRecipeUseCase  
**When** the creation is successful  
**Then** a RecipeCreatedEvent should be published with correct recipe details

**Given** a recipe is imported through ImportRecipeFromImageUseCase  
**When** the import is successful  
**Then** a RecipeImportedEvent should be published with extraction details

**Given** a recipe is updated through UpdateRecipeUseCase  
**When** the update is successful  
**Then** a RecipeUpdatedEvent should be published with before/after details

**Given** a recipe operation fails during execution  
**When** an exception occurs before event publishing  
**Then** no domain events should be published

**Given** an event handler throws an exception  
**When** the event is being processed  
**Then** the entire transaction should roll back

**Given** multiple events are generated in a single use case  
**When** the use case completes successfully  
**Then** all events should be published in the correct order

**Given** domain events are published  
**When** event handlers are executed  
**Then** they should run within the same transaction as the business operation

**Given** the event system is implemented  
**When** it is reviewed for coupling  
**Then** domain events should not depend on infrastructure concerns

## Testing Requirements

**Unit Tests:**

Create `RecipeCreatedEventTest.java`:
- Test event creation with valid data
- Test event property access
- Test event equality and hash code
- Test event serialization if needed

Create `RecipeImportedEventTest.java`:
- Test event creation with import details
- Test extraction source tracking
- Test ingredient/instruction counts

Create `DomainEventPublisherTest.java`:
- Test single event publishing
- Test multiple event publishing
- Test event handler invocation
- Mock event infrastructure

Create `RecipeAuditEventHandlerTest.java`:
- Test audit logging for all recipe events
- Test log message format and content
- Test error handling in event handlers

**Integration Tests:**

Create `DomainEventIntegrationTest.java`:
- Test end-to-end event publishing from use cases
- Test transaction rollback with event handler failures
- Test event handler execution order
- Test event publishing within transaction boundaries

Create `RecipeEventWorkflowTest.java`:
- Test complete recipe lifecycle with events
- Test event publishing for create/update/import/delete operations
- Test event handler interactions
- Test audit trail generation

**Event Contract Tests:**

Create `RecipeEventContractTest.java`:
- Test event structure and properties
- Test event backward compatibility
- Test event handler interface compliance
- Verify event type registration

**Test Coverage Requirements:**
- All domain events must have unit tests
- All event handlers must have unit tests
- Event publishing integration must be tested
- Transaction behavior with events must be verified
- Event handler failure scenarios must be covered