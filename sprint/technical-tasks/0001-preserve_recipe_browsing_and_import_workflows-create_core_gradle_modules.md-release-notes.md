## Summary
Created the Java 25 core Gradle modules for clean architecture with proper dependency management and isolated testing.

## Changes
- Created `core/domain` and `core/usecases` Gradle modules
- Updated `settings.gradle.kts` to include both core modules
- Created proper build files for core modules using Gradle Groovy DSL
- Added JUnit 5 test dependencies to core modules for isolated testing
- Backend now depends on core modules via implementation dependencies

## Files Changed
- `settings.gradle.kts` - Added core module includes
- `backend/build.gradle.kts` - Added core module dependencies
- `core/domain/build.gradle` - New file
- `core/usecases/build.gradle` - New file
- `core/domain/src/test/java/be/lutske/leolegacy/domain/CoreModuleSmokeTest.java` - New file
- `core/usecases/src/test/java/be/lutske/leolegacy/usecases/CoreModuleSmokeTest.java` - New file

## Verification
- `./gradlew projects` - Both core modules appear in project list
- `./gradlew :core:domain:test :core:usecases:test` - Tests pass on Java 25
- `./gradlew build` - Full project builds successfully

## Testing
- Smoke tests verify Java 25 runtime version
- Domain module has no external runtime dependencies
- Usecases module depends on domain module only

## Dependencies
- Gradle 9.3.1 with Java 25 toolchain
- JUnit 5.11.4 for testing

(End of file)