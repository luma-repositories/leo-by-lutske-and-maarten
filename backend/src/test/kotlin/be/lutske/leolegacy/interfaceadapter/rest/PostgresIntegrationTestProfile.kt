package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Quarkus test profile that uses the real PostgreSQL database (via podman-compose)
 * and real Tesseract OCR instead of H2 + mocked OCR.
 *
 * Prerequisite: `podman compose up -d` must be running.
 * Prerequisite: Tesseract must be installed (`brew install tesseract`).
 */
class PostgresIntegrationTestProfile : QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String, String> {
        return mapOf(
            // Point to real PostgreSQL (same as compose.yaml)
            "quarkus.datasource.db-kind" to "postgresql",
            "quarkus.datasource.username" to "leo",
            "quarkus.datasource.password" to "leo_secret",
            "quarkus.datasource.jdbc.url" to "jdbc:postgresql://localhost:5432/leo_legacy",

            // Flyway: clean + migrate to get a fresh schema each test run
            "quarkus.flyway.migrate-at-start" to "true",
            "quarkus.flyway.clean-at-start" to "true",

            // Real Tesseract OCR config
            "ocr.tessdata-path" to "/opt/homebrew/share/tessdata",
            "ocr.language" to "eng",
        )
    }
}
