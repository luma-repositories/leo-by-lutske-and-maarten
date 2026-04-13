package be.lutske.leolegacy.validation.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CleanArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("be.lutske.leolegacy");
    }

    @Test
    void domainShouldNotDependOnAnyFramework() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..",
                        "io.quarkus..",
                        "org.hibernate..",
                        "dev.langchain4j..",
                        "com.fasterxml..",
                        "org.eclipse.microprofile.."
                )
                .check(classes);
    }

    @Test
    void portsShouldNotDependOnAnyFramework() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.port..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..",
                        "io.quarkus..",
                        "org.hibernate..",
                        "dev.langchain4j..",
                        "com.fasterxml..",
                        "org.eclipse.microprofile.."
                )
                .check(classes);
    }

    @Test
    void usecasesShouldNotDependOnAnyFramework() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.usecase..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..",
                        "io.quarkus..",
                        "org.hibernate..",
                        "dev.langchain4j..",
                        "com.fasterxml..",
                        "org.eclipse.microprofile.."
                )
                .check(classes);
    }

    @Test
    void domainShouldNotDependOnOuterLayers() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "be.lutske.leolegacy.infrastructure..",
                        "be.lutske.leolegacy.entrypoint..",
                        "be.lutske.leolegacy.configuration..",
                        "be.lutske.leolegacy.usecase..",
                        "be.lutske.leolegacy.port.."
                )
                .check(classes);
    }

    @Test
    void portsShouldOnlyDependOnDomain() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.port..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "be.lutske.leolegacy.infrastructure..",
                        "be.lutske.leolegacy.entrypoint..",
                        "be.lutske.leolegacy.configuration..",
                        "be.lutske.leolegacy.usecase.."
                )
                .check(classes);
    }

    @Test
    void usecasesShouldNotDependOnInfrastructureOrEntrypoints() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.usecase..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "be.lutske.leolegacy.infrastructure..",
                        "be.lutske.leolegacy.entrypoint..",
                        "be.lutske.leolegacy.configuration.."
                )
                .check(classes);
    }

    @Test
    void infrastructureShouldNotDependOnEntrypointsOrConfiguration() {
        noClasses()
                .that().resideInAPackage("be.lutske.leolegacy.infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "be.lutske.leolegacy.entrypoint..",
                        "be.lutske.leolegacy.configuration.."
                )
                .check(classes);
    }
}
