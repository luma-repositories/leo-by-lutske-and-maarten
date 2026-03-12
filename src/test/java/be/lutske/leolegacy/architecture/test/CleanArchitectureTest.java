package be.lutske.leolegacy.architecture.test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importing.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CleanArchitectureTest {

    @Test
    void testLayerDependencies() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("be.lutske.leolegacy");

        // Layer Dependency Rules
        ArchRule domainLayerShouldNotDependOnInfrastructure = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..interfaceadapter..");

        ArchRule domainLayerShouldNotDependOnApplication = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..");

        ArchRule applicationLayerShouldNotDependOnInfrastructure = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..");

        ArchRule infrastructureLayerShouldNotDependOnDomain = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage("..domain..");

        // Package Structure Compliance
        ArchRule domainPackageShouldHaveNoSubpackages = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.domain").should().haveNoSubpackages();

        // Annotation Usage Patterns
        ArchRule repositoriesShouldBeAnnotatedWithRepository = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..infrastructure..repository..")
                .should().beAnnotatedWith("javax.persistence.Repository");

        // Interface vs Implementation Patterns
        ArchRule interfacesShouldBeInDomainInterfacesPackage = ArchRuleDefinition.noClasses()
                .that().areInterfaces()
                .should().resideInAPackage("..domain.interfaces..");

        ArchRule implementationsShouldBeInApplicationServicesPackage = ArchRuleDefinition.noClasses()
                .that().areNotInterfaces()
                .should().resideInAPackage("..application.services..");

        // Apply all rules
        domainLayerShouldNotDependOnInfrastructure.check(importedClasses);
        domainLayerShouldNotDependOnApplication.check(importedClasses);
        applicationLayerShouldNotDependOnInfrastructure.check(importedClasses);
        infrastructureLayerShouldNotDependOnDomain.check(importedClasses);
        domainPackageShouldHaveNoSubpackages.check(importedClasses);
        repositoriesShouldBeAnnotatedWithRepository.check(importedClasses);
        interfacesShouldBeInDomainInterfacesPackage.check(importedClasses);
        implementationsShouldBeInApplicationServicesPackage.check(importedClasses);
    }
}