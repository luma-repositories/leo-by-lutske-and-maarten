package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Quarkus test profile that uses the real PostgreSQL database (via podman-compose)
 * and mocked AI extraction service.
 *
 * Prerequisite: `podman compose up -d` must be running.
 */
class PostgresIntegrationTestProfile : QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String, String> {
        return mapOf(
            // Point to real PostgreSQL (same as compose.yaml)
            "quarkus.datasource.db-kind" to "postgresql",
            "quarkus.datasource.username" to "leo",
            "quarkus.datasource.password" to "leo_secret",
            "quarkus.datasource.jdbc.url" to "jdbc:postgresql://localhost:5432/leo_legacy",
            "quarkus.datasource.jdbc.acquisition-timeout" to "5",

            // Flyway: clean + migrate to get a fresh schema each test run
            "quarkus.flyway.migrate-at-start" to "true",
            "quarkus.flyway.clean-at-start" to "true",

            // AI config (extraction service is mocked in integration tests)
            "app.ai.provider" to "openai",
            "app.ai.model" to "gpt-4o-test",
            "app.ai.api-key" to "test-key-not-real",
        )
    }
}
