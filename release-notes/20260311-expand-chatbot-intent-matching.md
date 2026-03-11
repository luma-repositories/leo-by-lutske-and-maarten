# Expand Leonardo chatbot intent matching

## Summary

- Extended Leonardo's deterministic recipe matching with broader phrasing support such as asking for something with chicken.
- Added curated synonym and category alias matching so queries like pasta ideas also surface related recipes.
- Added regression coverage for the new natural-language matching paths.

## User impact

- Users can ask for recipes more naturally and still get relevant direct links to matching dishes.

## Config/env changes

- None.

## Platform dependency notes

- No new backend dependency versions were required; dependency governance remains centralized in `platform/quarkus-platform.gradle`.

## Migration notes

- None.

## How to verify

- Run `./gradlew clean build`.
- Run `./gradlew :backend:test`.
- Ask Leonardo for `something with chicken` and `I want pasta tonight` and confirm recommendations appear.
