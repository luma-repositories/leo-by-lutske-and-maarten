package be.lutske.leolegacy.validation.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GradleModuleDependencyTest {

    private static Path rootDir;

    @BeforeAll
    static void locateProjectRoot() {
        Path candidate = Path.of(System.getProperty("user.dir"));
        while (candidate != null && !Files.exists(candidate.resolve("settings.gradle.kts"))) {
            candidate = candidate.getParent();
        }
        if (candidate == null) {
            fail("Could not locate project root (no settings.gradle.kts found)");
        }
        rootDir = candidate;
    }

    // -----------------------------------------------------------------------
    // Module structure: verify all expected modules are declared in settings
    // -----------------------------------------------------------------------

    @Nested
    class ModuleStructure {

        private static final Set<String> EXPECTED_MODULES = Set.of(
                "backend",
                "backend:application",
                "backend:application:core",
                "backend:application:core:utils",
                "backend:application:core:domain",
                "backend:application:core:ports",
                "backend:application:core:usecases",
                "backend:application:infrastructure",
                "backend:application:infrastructure:persistence",
                "backend:application:infrastructure:persistence:postgres",
                "backend:application:infrastructure:persistence:in-memory",
                "backend:application:infrastructure:gateways",
                "backend:application:infrastructure:gateways:http-clients",
                "backend:application:apis",
                "backend:application:apis:jakarta-apis",
                "backend:application:configuration",
                "backend:application:configuration:quarkus-app",
                "backend:validation",
                "backend:validation:architecture",
                "backend:validation:integration-tests"
        );

        @Test
        void settingsContainsAllExpectedModules() throws IOException {
            String settings = readFile(rootDir.resolve("settings.gradle.kts"));
            for (String module : EXPECTED_MODULES) {
                assertTrue(settings.contains("\"" + module + "\""),
                        "settings.gradle.kts must include module: " + module);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "backend/application/core/utils",
                "backend/application/core/domain",
                "backend/application/core/ports",
                "backend/application/core/usecases",
                "backend/application/infrastructure/persistence/postgres",
                "backend/application/infrastructure/persistence/in-memory",
                "backend/application/infrastructure/gateways/http-clients",
                "backend/application/apis/jakarta-apis",
                "backend/application/configuration/quarkus-app",
                "backend/validation/architecture",
                "backend/validation/integration-tests"
        })
        void leafModuleHasBuildFile(String modulePath) {
            Path buildFile = rootDir.resolve(modulePath).resolve("build.gradle.kts");
            assertTrue(Files.exists(buildFile),
                    "Leaf module must have build.gradle.kts: " + modulePath);
        }
    }

    // -----------------------------------------------------------------------
    // Core module dependency constraints
    // -----------------------------------------------------------------------

    @Nested
    class CoreModuleDependencies {

        @Test
        void utilsHasNoProjectDependencies() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/core/utils");
            assertTrue(deps.isEmpty(),
                    "utils must have zero project dependencies, but found: " + deps);
        }

        @Test
        void domainOnlyDependsOnUtils() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/core/domain");
            Set<String> allowed = Set.of(":backend:application:core:utils");
            assertOnlyAllowedDependencies("domain", deps, allowed);
        }

        @Test
        void portsOnlyDependsOnDomain() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/core/ports");
            Set<String> allowed = Set.of(":backend:application:core:domain");
            assertOnlyAllowedDependencies("ports", deps, allowed);
        }

        @Test
        void usecasesOnlyDependsOnCoreSiblings() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/core/usecases");
            Set<String> allowed = Set.of(
                    ":backend:application:core:domain",
                    ":backend:application:core:ports",
                    ":backend:application:core:utils"
            );
            assertOnlyAllowedDependencies("usecases", deps, allowed);
        }

        @Test
        void utilsHasNoThirdPartyDependencies() throws IOException {
            List<String> libs = extractThirdPartyDependencies("backend/application/core/utils");
            assertTrue(libs.isEmpty(),
                    "utils must have no third-party dependencies, but found: " + libs);
        }

        @Test
        void domainHasNoThirdPartyDependencies() throws IOException {
            List<String> libs = extractThirdPartyDependencies("backend/application/core/domain");
            assertTrue(libs.isEmpty(),
                    "domain must have no third-party dependencies, but found: " + libs);
        }

        @Test
        void portsHasNoThirdPartyDependencies() throws IOException {
            List<String> libs = extractThirdPartyDependencies("backend/application/core/ports");
            assertTrue(libs.isEmpty(),
                    "ports must have no third-party dependencies, but found: " + libs);
        }

        @Test
        void usecasesHasNoNonTestThirdPartyDependencies() throws IOException {
            List<String> libs = extractNonTestThirdPartyDependencies("backend/application/core/usecases");
            assertTrue(libs.isEmpty(),
                    "usecases must have no non-test third-party dependencies, but found: " + libs);
        }
    }

    // -----------------------------------------------------------------------
    // Infrastructure module dependency constraints
    // -----------------------------------------------------------------------

    @Nested
    class InfrastructureModuleDependencies {

        @Test
        void postgresOnlyDependsOnCoreModules() throws IOException {
            List<String> deps = extractProjectDependencies(
                    "backend/application/infrastructure/persistence/postgres");
            assertNoDependencyOnModules("postgres", deps, Set.of(
                    ":backend:application:apis",
                    ":backend:application:configuration",
                    ":backend:application:infrastructure:gateways",
                    ":backend:application:infrastructure:persistence:in-memory",
                    ":backend:validation"
            ));
        }

        @Test
        void inMemoryOnlyDependsOnCoreModules() throws IOException {
            List<String> deps = extractProjectDependencies(
                    "backend/application/infrastructure/persistence/in-memory");
            Set<String> allowed = Set.of(
                    ":backend:application:core:domain",
                    ":backend:application:core:ports"
            );
            assertOnlyAllowedDependencies("in-memory", deps, allowed);
        }

        @Test
        void inMemoryHasNoThirdPartyDependencies() throws IOException {
            List<String> libs = extractThirdPartyDependencies(
                    "backend/application/infrastructure/persistence/in-memory");
            assertTrue(libs.isEmpty(),
                    "in-memory must have no third-party dependencies, but found: " + libs);
        }

        @Test
        void httpClientsOnlyDependsOnCoreModules() throws IOException {
            List<String> deps = extractProjectDependencies(
                    "backend/application/infrastructure/gateways/http-clients");
            assertNoDependencyOnModules("http-clients", deps, Set.of(
                    ":backend:application:apis",
                    ":backend:application:configuration",
                    ":backend:application:core:usecases",
                    ":backend:application:infrastructure:persistence",
                    ":backend:validation"
            ));
        }

        @Test
        void noInfrastructureModuleDependsOnApis() throws IOException {
            for (String infraModule : List.of(
                    "backend/application/infrastructure/persistence/postgres",
                    "backend/application/infrastructure/persistence/in-memory",
                    "backend/application/infrastructure/gateways/http-clients")) {
                List<String> deps = extractProjectDependencies(infraModule);
                for (String dep : deps) {
                    assertFalse(dep.contains(":apis:"),
                            infraModule + " must not depend on apis modules, but depends on: " + dep);
                }
            }
        }

        @Test
        void noInfrastructureModuleDependsOnConfiguration() throws IOException {
            for (String infraModule : List.of(
                    "backend/application/infrastructure/persistence/postgres",
                    "backend/application/infrastructure/persistence/in-memory",
                    "backend/application/infrastructure/gateways/http-clients")) {
                List<String> deps = extractProjectDependencies(infraModule);
                for (String dep : deps) {
                    assertFalse(dep.contains(":configuration:"),
                            infraModule + " must not depend on configuration, but depends on: " + dep);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // APIs module dependency constraints
    // -----------------------------------------------------------------------

    @Nested
    class ApisModuleDependencies {

        @Test
        void jakartaApisDoesNotDependOnInfrastructure() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/apis/jakarta-apis");
            for (String dep : deps) {
                assertFalse(dep.contains(":infrastructure:"),
                        "jakarta-apis must not depend on infrastructure modules, but depends on: " + dep);
            }
        }

        @Test
        void jakartaApisDoesNotDependOnConfiguration() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/apis/jakarta-apis");
            for (String dep : deps) {
                assertFalse(dep.contains(":configuration:"),
                        "jakarta-apis must not depend on configuration modules, but depends on: " + dep);
            }
        }

        @Test
        void jakartaApisOnlyDependsOnCoreModules() throws IOException {
            List<String> deps = extractProjectDependencies("backend/application/apis/jakarta-apis");
            Set<String> allowed = Set.of(
                    ":backend:application:core:domain",
                    ":backend:application:core:ports",
                    ":backend:application:core:usecases",
                    ":backend:application:core:utils"
            );
            assertOnlyAllowedDependencies("jakarta-apis", deps, allowed);
        }
    }

    // -----------------------------------------------------------------------
    // Configuration module dependency constraints
    // -----------------------------------------------------------------------

    @Nested
    class ConfigurationModuleDependencies {

        @Test
        void quarkusAppDoesNotDependOnInMemory() throws IOException {
            List<String> deps = extractProjectDependencies(
                    "backend/application/configuration/quarkus-app");
            for (String dep : deps) {
                assertFalse(dep.contains(":in-memory"),
                        "quarkus-app must not depend on in-memory (that's for tests), but depends on: " + dep);
            }
        }

        @Test
        void quarkusAppDoesNotDependOnValidation() throws IOException {
            List<String> deps = extractProjectDependencies(
                    "backend/application/configuration/quarkus-app");
            for (String dep : deps) {
                assertFalse(dep.contains(":validation:"),
                        "quarkus-app must not depend on validation modules, but depends on: " + dep);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Cross-cutting dependency direction rules
    // -----------------------------------------------------------------------

    @Nested
    class DependencyDirectionRules {

        @ParameterizedTest
        @ValueSource(strings = {
                "backend/application/core/utils",
                "backend/application/core/domain",
                "backend/application/core/ports",
                "backend/application/core/usecases"
        })
        void coreModulesDoNotDependOnInfrastructure(String modulePath) throws IOException {
            List<String> deps = extractProjectDependencies(modulePath);
            for (String dep : deps) {
                assertFalse(dep.contains(":infrastructure:"),
                        modulePath + " must not depend on infrastructure, but depends on: " + dep);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "backend/application/core/utils",
                "backend/application/core/domain",
                "backend/application/core/ports",
                "backend/application/core/usecases"
        })
        void coreModulesDoNotDependOnApis(String modulePath) throws IOException {
            List<String> deps = extractProjectDependencies(modulePath);
            for (String dep : deps) {
                assertFalse(dep.contains(":apis:"),
                        modulePath + " must not depend on apis, but depends on: " + dep);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "backend/application/core/utils",
                "backend/application/core/domain",
                "backend/application/core/ports",
                "backend/application/core/usecases"
        })
        void coreModulesDoNotDependOnConfiguration(String modulePath) throws IOException {
            List<String> deps = extractProjectDependencies(modulePath);
            for (String dep : deps) {
                assertFalse(dep.contains(":configuration:"),
                        modulePath + " must not depend on configuration, but depends on: " + dep);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "backend/application/core/utils",
                "backend/application/core/domain",
                "backend/application/core/ports",
                "backend/application/core/usecases",
                "backend/application/infrastructure/persistence/postgres",
                "backend/application/infrastructure/persistence/in-memory",
                "backend/application/infrastructure/gateways/http-clients",
                "backend/application/apis/jakarta-apis",
                "backend/application/configuration/quarkus-app"
        })
        void noApplicationModuleDependsOnValidation(String modulePath) throws IOException {
            List<String> deps = extractProjectDependencies(modulePath);
            for (String dep : deps) {
                assertFalse(dep.contains(":validation:"),
                        modulePath + " must not depend on validation modules, but depends on: " + dep);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static final Pattern PROJECT_DEP_PATTERN =
            Pattern.compile("project\\(\"([^\"]+)\"\\)");

    private static final Pattern THIRD_PARTY_DEP_PATTERN =
            Pattern.compile("(?:implementation|api|compileOnly)\\(\"([^:\"]+:[^:\"]+(?::[^\"]+)?)\"\\)");

    private static final Pattern NON_TEST_THIRD_PARTY_DEP_PATTERN =
            Pattern.compile("^\\s+(?:implementation|api|compileOnly)\\(\"([^:\"]+:[^:\"]+(?::[^\"]+)?)\"\\)");

    private static List<String> extractProjectDependencies(String modulePath) throws IOException {
        String content = readFile(rootDir.resolve(modulePath).resolve("build.gradle.kts"));
        return PROJECT_DEP_PATTERN.matcher(content).results()
                .map(m -> m.group(1))
                .toList();
    }

    private static List<String> extractThirdPartyDependencies(String modulePath) throws IOException {
        String content = readFile(rootDir.resolve(modulePath).resolve("build.gradle.kts"));
        return content.lines()
                .filter(line -> !line.stripLeading().startsWith("//"))
                .filter(line -> !line.contains("enforcedPlatform"))
                .filter(line -> !line.contains("testImplementation"))
                .filter(line -> !line.contains("testRuntimeOnly"))
                .flatMap(line -> THIRD_PARTY_DEP_PATTERN.matcher(line).results())
                .map(m -> m.group(1))
                .toList();
    }

    private static List<String> extractNonTestThirdPartyDependencies(String modulePath) throws IOException {
        String content = readFile(rootDir.resolve(modulePath).resolve("build.gradle.kts"));
        return content.lines()
                .filter(line -> !line.stripLeading().startsWith("//"))
                .filter(line -> !line.contains("enforcedPlatform"))
                .filter(line -> !line.contains("testImplementation"))
                .filter(line -> !line.contains("testRuntimeOnly"))
                .flatMap(line -> NON_TEST_THIRD_PARTY_DEP_PATTERN.matcher(line).results())
                .map(m -> m.group(1))
                .toList();
    }

    private static String readFile(Path path) throws IOException {
        assertTrue(Files.exists(path), "File must exist: " + path);
        return Files.readString(path);
    }

    private static void assertOnlyAllowedDependencies(String moduleName,
                                                       List<String> actual,
                                                       Set<String> allowed) {
        Set<String> disallowed = actual.stream()
                .filter(dep -> !allowed.contains(dep))
                .collect(Collectors.toSet());
        assertTrue(disallowed.isEmpty(),
                moduleName + " has disallowed project dependencies: " + disallowed
                        + ". Allowed: " + allowed);
    }

    private static void assertNoDependencyOnModules(String moduleName,
                                                     List<String> actual,
                                                     Set<String> forbidden) {
        for (String dep : actual) {
            for (String forbiddenPrefix : forbidden) {
                assertFalse(dep.startsWith(forbiddenPrefix),
                        moduleName + " must not depend on " + forbiddenPrefix + ", but depends on: " + dep);
            }
        }
    }
}
