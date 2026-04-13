Refactor the backend Gradle module.
It should be multi-module clean architecture, like it is outlined in this demo project: https://github.com/maarten-vandeperre/clean-architecture-software-sample-project

you should have a structure like this in gradle submodules:
/backend
    /application
        /configuration
            /quarkus-app
        /core
            /utils
            /domain
            /usecases
            /ports
        /infrastructure
            /persistence
                /postgres
                /in-memory
            /gateways
                /http-clients
        /apis
            /jakarta-apis
    /validation
        /architecture
        /integration-tests

core should not contain any dependency, beyond the programming language. domain can depend on utils, usecases on domain, ports and utils
validation/architecture should contain archunit tests
integration-tests should contain integration tests on the REST APIs, going to the in-memory database implementation of the repositories, which can be populated/manipulated in the integrations tests,
where there is a implementation per test context, so that parallel tests are not conflicting with each other
        