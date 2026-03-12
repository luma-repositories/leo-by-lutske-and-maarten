# Create architectural testing

## Related User Story

User Story: refactor_backend_for_clean_architecture

## Objective

Implement architectural compliance tests using ArchUnit to verify clean architecture principles and enforce layer dependencies, ensuring the codebase maintains proper separation of concerns.

## Scope

- Create CleanArchitectureTest class with ArchUnit rules
- Implement layer dependency verification
- Create package structure compliance tests
- Verify annotation usage patterns
- Ensure interface vs implementation patterns

## Out of Scope

- Domain testing framework (separate task)
- Use case testing framework (separate task)
- Integration testing setup
- Performance testing

## Implementation Details

### CleanArchitectureTest Class

Create `be.lutske.leolegacy.architecture.test.CleanArchitectureTest`:

```java
@AnalyzeClasses(packages = "be.lutske.leolegacy")
public class CleanArchitectureTest {
    
    // Layer Dependency Rules
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
    
    // Repository Pattern Rules
    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces = 
        classes()
            .that().resideInAPackage("..domain.repository..")
            .and().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces();
    
    @ArchTest
    static final ArchRule repositoryImplementationsShouldBeInInfrastructure = 
        classes()
            .that().implement(JavaClass.Predicates.resideInAPackage("..domain.repository.."))
            .and().areNotInterfaces()
            .should().resideInAPackage("..infrastructure..");
    
    // Use Case Rules
    @ArchTest
    static final ArchRule useCasesShouldBeAnnotatedWithTransactional = 
        classes()
            .that().resideInAPackage("..application.usecase..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beAnnotatedWith(Transactional.class);
    
    @ArchTest
    static final ArchRule useCasesShouldNotBePublic = 
        classes()
            .that().resideInAPackage("..application.usecase..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().bePackagePrivate();
    
    // Domain Entity Rules
    @ArchTest
    static final ArchRule domainEntitiesShouldNotDependOnFrameworks = 
        noClasses()
            .that().resideInAPackage("..domain.entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("javax.persistence..", "org.springframework..", "io.quarkus..");
    
    @ArchTest
    static final ArchRule domainEntitiesShouldNotHaveSetters = 
        noClasses()
            .that().resideInAPackage("..domain.entity..")
            .should().haveMethodsThat()
            .haveName(name -> name.startsWith("set") && name.length() > 3);
    
    // Value Object Rules
    @ArchTest
    static final ArchRule valueObjectsShouldBeImmutable = 
        classes()
            .that().resideInAPackage("..domain.valueobject..")
            .should().haveOnlyFinalFields();
    
    // Event Rules
    @ArchTest
    static final ArchRule domainEventsShouldBeImmutable = 
        classes()
            .that().resideInAPackage("..domain.event..")
            .and().haveSimpleNameEndingWith("Event")
            .should().haveOnlyFinalFields();
    
    @ArchTest
    static final ArchRule domainEventsShouldImplementDomainEvent = 
        classes()
            .that().resideInAPackage("..domain.event..")
            .and().haveSimpleNameEndingWith("Event")
            .should().implement(DomainEvent.class);
    
    // Service Rules
    @ArchTest
    static final ArchRule domainServicesShouldBeInDomainLayer = 
        classes()
            .that().haveSimpleNameEndingWith("DomainService")
            .should().resideInAPackage("..domain.service..");
    
    @ArchTest
    static final ArchRule applicationServicesShouldBeInApplicationLayer = 
        classes()
            .that().haveSimpleNameEndingWith("ApplicationService")
            .should().resideInAPackage("..application.service..");
    
    // Controller Rules
    @ArchTest
    static final ArchRule controllersShouldBeInInterfaceAdapterLayer = 
        classes()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().resideInAPackage("..interfaceadapter.web..");
    
    @ArchTest
    static final ArchRule controllersShouldNotDependOnDomainDirectly = 
        noClasses()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().dependOnClassesThat()
            .resideInAnyPackage("..domain.entity..", "..domain.repository..", "..domain.service..");
    
    // Package Structure Rules
    @ArchTest
    static final ArchRule packagesShouldFollowNamingConvention = 
        classes()
            .should().resideInAnyPackage(
                "..domain..",
                "..application..",
                "..infrastructure..",
                "..interfaceadapter..",
                "..config..",
                "..test.."
            );
}
```

### Additional Architectural Tests

Create `be.lutske.leolegacy.architecture.test.NamingConventionTest`:

```java
@AnalyzeClasses(packages = "be.lutske.leolegacy")
public class NamingConventionTest {
    
    @ArchTest
    static final ArchRule repositoryInterfacesShouldEndWithRepository = 
        classes()
            .that().resideInAPackage("..domain.repository..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Repository");
    
    @ArchTest
    static final ArchRule useCasesShouldEndWithUseCase = 
        classes()
            .that().resideInAPackage("..application.usecase..")
            .should().haveSimpleNameEndingWith("UseCase");
    
    @ArchTest
    static final ArchRule domainEventsShouldEndWithEvent = 
        classes()
            .that().resideInAPackage("..domain.event..")
            .should().haveSimpleNameEndingWith("Event");
    
    @ArchTest
    static final ArchRule valueObjectsShouldNotEndWithEntity = 
        noClasses()
            .that().resideInAPackage("..domain.valueobject..")
            .should().haveSimpleNameEndingWith("Entity");
    
    @ArchTest
    static final ArchRule entitiesShouldNotEndWithDto = 
        noClasses()
            .that().resideInAPackage("..domain.entity..")
            .should().haveSimpleNameEndingWith("Dto");
}
```

### Test Dependencies

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.0.1</version>
    <scope>test</scope>
</dependency>
```

## Files / Modules Impacted

**New files to create:**
- `be/lutske/leolegacy/architecture/test/CleanArchitectureTest.java`
- `be/lutske/leolegacy/architecture/test/NamingConventionTest.java`

**Dependencies to add:**
- ArchUnit for architectural testing

**Directory structure:**
```
be/lutske/leolegacy/architecture/test/
├── CleanArchitectureTest.java
└── NamingConventionTest.java
```

## Acceptance Criteria

**Given** architectural tests are implemented  
**When** they are executed  
**Then** they should verify clean architecture compliance

**Given** layer dependency rules are implemented  
**When** code violates layer boundaries  
**Then** tests should fail with clear error messages

**Given** naming convention tests are implemented  
**When** classes don't follow naming conventions  
**Then** tests should fail and indicate the violation

**Given** repository pattern rules are implemented  
**When** repositories are not properly structured  
**Then** tests should enforce interface-based design

**Given** domain entity rules are implemented  
**When** entities depend on frameworks or have setters  
**Then** tests should prevent framework coupling

## Testing Requirements

**Architectural Compliance Tests:**

The architectural tests themselves serve as the testing mechanism:
- Layer dependency verification
- Package structure compliance
- Naming convention enforcement
- Annotation usage verification
- Interface vs implementation patterns

**Test Coverage Requirements:**
- All architectural rules must be tested
- Layer boundaries must be enforced
- Naming conventions must be verified
- Framework coupling must be prevented
- Clean architecture principles must be maintained

**Execution Requirements:**
- Tests should run as part of the regular test suite
- Tests should fail fast when violations are detected
- Error messages should clearly indicate the violation
- Tests should be maintainable as the codebase evolves