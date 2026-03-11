# Add chatbot memory and stronger ranking

## Summary

- Improved Leonardo's deterministic ranking so exact title and category matches are prioritized over broader synonym-only hits.
- Added lightweight in-session conversation context so follow-up prompts like `make it vegetarian` can refine the previous question.
- Added backend and frontend regression coverage for context-aware chat requests and ranking behavior.

## User impact

- Users get more precise first results and can refine recipe suggestions naturally without restating the whole request.

## Config/env changes

- None.

## Platform dependency notes

- No new backend dependency versions were required; dependency governance remains centralized in `platform/quarkus-platform.gradle`.

## Migration notes

- None.

## How to verify

- Run `./gradlew clean build`.
- Run `./gradlew :backend:test`.
- Ask Leonardo for `pizza` and confirm pizza is ranked ahead of broader pasta matches.
- Ask for `I would like a tomato based dish`, then follow with `make it vegetarian`, and confirm Leonardo refines the suggestions.
