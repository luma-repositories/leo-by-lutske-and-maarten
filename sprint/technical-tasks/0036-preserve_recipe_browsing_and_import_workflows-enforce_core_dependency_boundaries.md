# Title
Enforce pure-core and JDBC-only build boundaries

## Related User Story
[preserve_recipe_browsing_and_import_workflows](../processed-user-stories/preserve_recipe_browsing_and_import_workflows.md).

## Objective
Make framework leakage into the extracted core and reintroduction of ORM fail verification automatically.

## Scope
One Gradle verification script, attached to the existing module `check` tasks, with deterministic dependency and production-source checks.

## Out of Scope
Business behavior changes, new application modules, frontend restructuring, Java module descriptors, migrations, or adding an architecture library to production dependencies.

## Clean Architecture Placement
Testing/build infrastructure. Domain and usecases remain pure Java; backend is the outer Quarkus composition and infrastructure module. Follow https://github.com/maarten-vandeperre/clean-architecture-software-sample-project.

## Execution Dependencies
- `0025-preserve_recipe_browsing_and_import_workflows-remove_orm_and_unused_business_logic.md`

## Implementation Details
- Add `gradle/recipe-architecture-checks.gradle` as a Groovy script plugin, applied from both core build files and the backend build. Resolve project locations via Gradle project APIs, never absolute developer-machine paths.
- Register `verifyRecipeArchitecture` under the verification group in each project and attach it to that project's `check`. Inspect production `compileClasspath` and `runtimeClasspath` resolved components only; test-only JUnit, Quarkus Test, H2, and Testcontainers are not violations.
- Domain must resolve no external module component or project dependency. Usecases may resolve only project `:core:domain`, with no external components. Backend must depend inward on core and must not resolve ORM modules whose group is `org.hibernate.orm`, whose module starts with `quarkus-hibernate-orm`, or whose module is `quarkus-panache-common`. Keep JDBC, Flyway, datasource pooling, REST, and AI libraries allowed in backend.
- Scan `core/domain/src/main/java` and `core/usecases/src/main/java` Java import declarations. Allow only `java.lang`, `java.util`, `java.time`, `java.math`, and these exact package prefixes' subpackages, plus `be.lutske.leolegacy.domain` and, for usecases only, `be.lutske.leolegacy.usecases`. Treat static imports by their declaring package. Disallow outward imports, `java.sql`, `javax.sql`, `java.io`, `java.nio.file`, `java.net`, framework/JSON/logging imports and wildcard imports outside allowed prefixes. Empty source trees are valid during initial setup. Do not scan test sources or generated build directories.
- Scan backend production Java import declarations to reject `jakarta.persistence`, `javax.persistence`, `org.hibernate`, and `io.quarkus.hibernate.orm` prefixes. Do not reject plain `java.sql` or `javax.sql` there.
- These checks supplement isolated compilation, not claim a complete Java semantic or fully-qualified-reference analyzer. Code review must still reject hidden infrastructure access and fully qualified forbidden references in core.
- Fail with the project name and offending dependency or source path/import. Do not silently filter forbidden dependencies, rewrite source, or alter application dependency versions to pass the check.

## Files / Modules Impacted
- `gradle/recipe-architecture-checks.gradle` (new; Gradle creates no application source here).
- `core/domain/build.gradle.kts`
- `core/usecases/build.gradle.kts`
- `backend/build.gradle.kts`

## Acceptance Criteria
- Given the completed core extraction and ORM removal, when all three verification tasks run, then they pass without starting Quarkus, AI, or a database.
- Given an external production dependency or outward import in core, when its verification task runs, then it fails and identifies the violation.
- Given test-only framework dependencies, when verification runs, then those dependencies do not contaminate or fail the production-core checks.
- Given ORM on backend's production classpath, when backend verification runs, then it fails while the JDBC driver remains permitted.

## Testing Requirements
Run `./gradlew :core:domain:verifyRecipeArchitecture :core:usecases:verifyRecipeArchitecture :backend:verifyRecipeArchitecture`. Verify negative cases in a disposable copy of the Gradle fixture, never by leaving forbidden dependencies in this repository: a domain external dependency, a usecase-to-backend import, a core `java.sql` import, and a backend ORM import must each fail; a JUnit test import must not. Run `./gradlew :core:domain:check :core:usecases:check :backend:check -x :backend:buildFrontend -x :backend:copyFrontend` to verify task attachment and all ordinary tests. No integration or live-provider invocation is required by this task.

## Dependencies / Preconditions
JDK 25 and existing Gradle dependency resolution. Production core modules and ORM removal have been implemented by the execution prerequisite.
