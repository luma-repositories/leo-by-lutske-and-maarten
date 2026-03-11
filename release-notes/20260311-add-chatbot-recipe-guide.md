# Add Leonardo chatbot recipe guide

## Summary

- Added a Leonardo chatbot to the homepage so visitors can ask for recipe ideas in natural language.
- Added backend recipe matching and a new chatbot API endpoint that returns direct recipe recommendations.
- Added frontend tests, backend tests, and homepage wiring for the new guided recipe discovery flow.

## User impact

- Users can ask for ingredient- or flavor-based suggestions, such as tomato-based dishes, and open matching recipes directly from the chat panel.

## Config/env changes

- None.

## Platform dependency notes

- No new backend dependency versions were required; dependency governance remains centralized in `platform/quarkus-platform.gradle`.

## Migration notes

- None.

## How to verify

- Run `./gradlew clean build`.
- Run `./gradlew :backend:test`.
- In `frontend/`, run `npm run lint`, `npm run test`, and `npm run build`.
- Open the homepage and ask Leonardo for a tomato-based dish; confirm recipe suggestions link to recipe detail pages.
