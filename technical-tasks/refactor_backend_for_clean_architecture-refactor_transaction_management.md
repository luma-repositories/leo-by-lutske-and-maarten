# Refactor transaction management to use case boundaries

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Move transaction boundaries from REST controllers to use case level, ensuring proper transaction scope and improving separation of concerns between interface adapters and application logic.

## Scope

- Remove @Transactional annotations from REST controllers
- Add @Transactional annotations to use case classes
- Configure appropriate transaction propagation and isolation levels
- Implement proper rollback strategies for business exceptions
- Ensure transaction boundaries align with business operations
- Maintain existing transactional behavior for end users

## Out of Scope

- Database connection configuration changes
- Transaction manager configuration changes
- Use case implementation changes (separate task)
- Repository implementation changes (separate task)

## Implementation Details

### Current Transaction Management Issues

**Problems to address:**
- `@Transactional` annotations scattered across REST controllers
- Transaction boundaries not aligned with business operations
- Infrastructure concerns mixed with business logic
- Inconsistent transaction propagation behavior
- Error handling not properly integrated with transaction rollback

### Use Case Transaction Configuration

**Transaction Annotation Strategy:**

Apply `@Transactional` to use case classes with appropriate configuration:

**Read-Only Use Cases:**
- `GetRecipeUseCase` → `@Transactional(readOnly = true)`
- `SearchRecipesUseCase` → `@Transactional(readOnly = true)`
- `GetAllCategoriesUseCase` → `@Transactional(readOnly = true)`

**Write Use Cases:**
- `CreateRecipeUseCase` → `@Transactional`
- `UpdateRecipeUseCase` → `@Transactional`
- `DeleteRecipeUseCase` → `@Transactional`
- `ImportRecipeFromImageUseCase` → `@Transactional`
- `CreateCategoryUseCase` → `@Transactional`

### Transaction Configuration Details

**CreateRecipeUseCase Transaction:**
```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.READ_COMMITTED,
    rollbackFor = {UseCaseException.class, DomainException.class}
)
```

**UpdateRecipeUseCase Transaction:**
```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.READ_COMMITTED,
    rollbackFor = {UseCaseException.class, DomainException.class}
)
```

**ImportRecipeFromImageUseCase Transaction:**
```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.READ_COMMITTED,
    timeout = 30, // AI operations may take longer
    rollbackFor = {UseCaseException.class, DomainException.class, RecipeImportException.class}
)
```

**Read-Only Use Cases:**
```java
@Transactional(
    readOnly = true,
    propagation = Propagation.SUPPORTS,
    isolation = Isolation.READ_COMMITTED
)
```

### Exception Handling and Rollback Strategy

**Rollback Configuration:**

**Business Exceptions (should trigger rollback):**
- `UseCaseException` and all subclasses
- `DomainException` and all subclasses
- `InvalidRecipeDataException`
- `RecipeImportException`

**Technical Exceptions (should trigger rollback):**
- `DataAccessException`
- `PersistenceException`
- `RuntimeException` (default behavior)

**Non-Rollback Exceptions:**
- `RecipeNotFoundException` (read operation, no state change)
- `CategoryNotFoundException` (read operation, no state change)

### Controller Transaction Removal

**Files to modify:**

**RecipeResource.java:**
- Remove all `@Transactional` annotations
- Remove transaction-related imports
- Ensure methods delegate to use cases without transaction management

**CategoryResource.java:**
- Remove all `@Transactional` annotations
- Remove transaction-related imports

**RecipeImportResource.java:**
- Remove all `@Transactional` annotations
- Remove transaction-related imports

### Transaction Testing Strategy

**Transaction Boundary Verification:**

Create transaction test utilities to verify:
- Use cases execute within proper transaction scope
- Read-only operations use read-only transactions
- Write operations use read-write transactions
- Rollback behavior works correctly for business exceptions
- Transaction propagation works as expected

**Integration Test Scenarios:**

1. **Successful Transaction Commit:**
   - Execute use case with valid data
   - Verify data is persisted after transaction commit
   - Verify transaction is committed at use case boundary

2. **Transaction Rollback on Business Exception:**
   - Execute use case that throws business exception
   - Verify no data is persisted after rollback
   - Verify transaction is rolled back properly

3. **Transaction Rollback on Technical Exception:**
   - Simulate database constraint violation
   - Verify transaction is rolled back
   - Verify proper exception propagation

4. **Read-Only Transaction Verification:**
   - Execute read-only use case
   - Verify no write operations are allowed
   - Verify read-only transaction optimization

### Performance Considerations

**Transaction Optimization:**

- Use read-only transactions for query operations to enable database optimizations
- Set appropriate timeout values for long-running operations (AI import)
- Configure proper isolation levels to balance consistency and performance
- Minimize transaction scope to reduce lock contention

**Connection Pool Configuration:**

Ensure transaction configuration aligns with connection pool settings:
- Transaction timeout < connection pool timeout
- Proper connection cleanup on transaction completion
- Adequate pool size for concurrent transactions

## Files / Modules Impacted

**Files to modify:**
- `be/lutske/leolegacy/application/usecase/CreateRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/UpdateRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/DeleteRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/GetRecipeUseCase.java`
- `be/lutske/leolegacy/application/usecase/SearchRecipesUseCase.java`
- `be/lutske/leolegacy/application/usecase/ImportRecipeFromImageUseCase.java`
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/CategoryResource.java`
- `be/lutske/leolegacy/interfaceadapter/rest/RecipeImportResource.java`

**New files to create:**
- `be/lutske/leolegacy/application/usecase/config/TransactionConfiguration.java` (if needed)

## Acceptance Criteria

**Given** use cases are annotated with @Transactional  
**When** a use case is executed  
**Then** it should run within a proper transaction boundary

**Given** REST controllers have @Transactional removed  
**When** a controller method is called  
**Then** it should not manage transactions directly

**Given** a write use case throws a business exception  
**When** the exception is thrown during execution  
**Then** the transaction should be rolled back and no data should be persisted

**Given** a read-only use case is executed  
**When** it performs query operations  
**Then** it should use a read-only transaction for optimization

**Given** an ImportRecipeFromImageUseCase is executed  
**When** AI extraction takes longer than normal  
**Then** the transaction timeout should accommodate the operation

**Given** transaction boundaries are moved to use cases  
**When** existing API operations are performed  
**Then** the transactional behavior should remain the same for end users

**Given** multiple repository operations occur in a single use case  
**When** the use case is executed  
**Then** all operations should participate in the same transaction

**Given** a use case fails with a technical exception  
**When** the exception occurs during execution  
**Then** the transaction should be rolled back automatically

## Testing Requirements

**Unit Tests:**

Create `TransactionBoundaryTest.java`:
- Test transaction annotations are present on use cases
- Test transaction configuration parameters
- Test rollback exception configuration
- Verify read-only transaction setup

**Integration Tests:**

Create `TransactionIntegrationTest.java`:
- Test successful transaction commit scenarios
- Test transaction rollback on business exceptions
- Test transaction rollback on technical exceptions
- Test read-only transaction behavior
- Test transaction timeout behavior
- Test concurrent transaction handling

**Transaction Verification Tests:**

Create `TransactionVerificationTest.java`:
- Use transaction synchronization to verify transaction state
- Test transaction propagation between use cases
- Verify transaction isolation levels
- Test connection cleanup after transaction completion

**Performance Tests:**

Create `TransactionPerformanceTest.java`:
- Test read-only transaction performance benefits
- Test transaction timeout under load
- Verify connection pool behavior with transactions
- Test concurrent transaction throughput

**Test Coverage Requirements:**
- All use cases must have transaction boundary tests
- All rollback scenarios must be tested
- Transaction configuration must be verified
- Performance impact must be measured
- Integration tests must verify end-to-end transaction behavior