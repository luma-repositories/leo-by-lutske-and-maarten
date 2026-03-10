package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Quarkus test profile that uses the real PostgreSQL database (via podman-compose)
 * and real Tesseract OCR instead of H2 + mocked OCR.
 *
 * Prerequisite: {@code podman compose up -d} must be running.
 * Prerequisite: Tesseract must be installed ({@code brew install tesseract}).
 */
public class PostgresIntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.datasource.username", "leo",
                "quarkus.datasource.password", "leo_secret",
                "quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:5432/leo_legacy",
                "quarkus.datasource.jdbc.acquisition-timeout", "5",
                "quarkus.flyway.migrate-at-start", "true",
                "quarkus.flyway.clean-at-start", "true",
                "ocr.tessdata-path", "/opt/homebrew/share/tessdata",
                "ocr.language", "eng"
        );
    }
}
