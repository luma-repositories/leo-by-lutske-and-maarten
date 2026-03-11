# Add chatbot suggestion chips and more recipe intents

## Summary

- Added clickable Leonardo suggestion chips for common recipe prompts on the homepage.
- Expanded deterministic chatbot intent matching with veggie, light, spicy, and oven-dish aliases.
- Added backend and frontend regression coverage for the new guided discovery paths.

## User impact

- Users can discover recipes faster by tapping suggested prompts or by using broader culinary phrasing.

## Config/env changes

- None.

## Platform dependency notes

- No new backend dependency versions were required; dependency governance remains centralized in `platform/quarkus-platform.gradle`.

## Migration notes

- None.

## How to verify

- Run `./gradlew clean build`.
- Run `./gradlew :backend:test`.
- In the chatbot, click a suggestion chip such as `Give me something spicy` and confirm recommendations appear.
- Ask Leonardo for a veggie idea or an oven dish and confirm relevant matches are returned.
