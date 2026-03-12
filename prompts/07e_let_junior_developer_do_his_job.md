# Prompt
_The given prompts where executed one by one_

Do your thing for refactor_backend_for_clean_architecture-create_domain_entities

Do your thing for refactor_backend_for_clean_architecture-create_domain_repository_interfaces

Do your thing for refactor_backend_for_clean_architecture-create_infrastructure_adapters

Do your thing for refactor_backend_for_clean_architecture-enhance_testing_architecture
==> qwen was failing on this one, so I asked the enterprise architect agent to split this into smaller tasks


Do your thing for 0010-refactor_backend_for_clean_architecture-create_domain_testing_framework

Do your thing for 0011-refactor_backend_for_clean_architecture-create_repository_test_doubles

Do your thing for 0012-refactor_backend_for_clean_architecture-create_use_case_testing_framework

Do your thing for 0013-refactor_backend_for_clean_architecture-create_domain_event_testing_framework
==> to Enterprise architect: 0013 is not clear for the developer. he has this question: I'm a bit unclear about how to proceed. Are you asking me to continue working on the technical task related to refactoring the backend and testing framework, or are you pointing out an issue with file attachments that need to be addressed first? Please clarify your request so I can assist you effectively.
==> Answer: Answer to your question: You should continue working on this technical task. The implementation is about creating testing infrastructure, not addressing file attachment issues. Start with creating the missing MockDomainEventPublisher dependency, then proceed with the main framework classes as outlined in the task specification.

Do your thing for 0014-refactor_backend_for_clean_architecture-create_architectural_testing

Do your thing for 0015-refactor_backend_for_clean_architecture-create_integration_testing_framework
=> ask the enterprise architect to have a look why the project is not building anymore and assign it in tasks for the developer
=> prompt to list the tickets to make the build succeed




Do your thing for 0017-fix_quarkus_build-add_quarkus_plugin_and_bom

Do your thing for 0018-fix_quarkus_build-replace_spring_with_quarkus_dependencies

Do your thing for 0019-fix_quarkus_build-verify_quarkus_build_success

= Question from junior developer: 
The attempt to fetch the repository from the provided URL resulted in a 404 error, indicating the URL might be invalid or the repository is not publicly accessible. To proceed, could you clarify the following:
1. Repository Access: Is the repository hosted privately, or should the URL be adjusted? If it's a local project, we can focus on the files you've already described.
2. Build Configuration: Are you confident the build.gradle.kts file includes all required Quarkus extensions (e.g., quarkus-hibernate-orm, quarkus-resteasy, quarkus-jdbc-h2) and LangChain4j dependencies?
3. Dependency Conflicts: Are there any specific error messages during the build that indicate missing or conflicting libraries?
   This will help resolve the issue without relying on external fetches.

Do your thing for 0020-fix_quarkus_build-remove_conflicting_build_file

Do your thing for 0019-fix_quarkus_build-verify_quarkus_build_success
=> Ask enterprise architect to fix the gradle clean build command

Do your thing for 0021-fix_compilation_errors-update_jpa_imports_and_entity_issues



Do your thing for refactor_backend_for_clean_architecture-implement_domain_events

Do your thing for refactor_backend_for_clean_architecture-implement_recipe_use_cases

Do your thing for refactor_backend_for_clean_architecture-refactor_rest_controllers

Do your thing for refactor_backend_for_clean_architecture-refactor_transaction_management

Do your thing for refactor_backend_for_clean_architecture-update_architecture_documentation