# Prompts

I need a bit more product context before I can turn this into a functional user story.
- Who is the primary user affected by this change: end user, admin, internal operations team, or developers only?
=> Internal developers mainly.
- What business problem should this change solve from the user’s perspective?
=> Easier to maintain and extend the application. Less dependent on libraries, which is a security feature and innovation solution
- Which user-facing capabilities or workflows are expected to improve after this work?
=> User would not be aware of this change normally
- Are there any specific screens, journeys, or actions in the application that must keep working exactly as they do today?
=> All screen should keep on behaving the same, as this is purely a technical story
- Should this be written as a purely internal quality story, or is there a visible functional outcome for users?
=> Internal quality
- What would success look like in business terms: faster delivery, fewer defects, easier feature changes, better reliability, easier onboarding, or something else?
=> faster delivery, easier future change, easier onboarding, fewer defects (less shared code among user requests), easier maintenance
- Are there any constraints or non-functional expectations that should be captured functionally, such as stability during release, no disruption to existing users, or no change to current behavior?
=> all tests should be green and application is able to bootstrap, no dependencies are allowed in the core (sub)modules (domain and usecases), only pure Java
- Should the resulting story cover the whole backend at once, or a specific domain/workflow first?
=> the whole backend