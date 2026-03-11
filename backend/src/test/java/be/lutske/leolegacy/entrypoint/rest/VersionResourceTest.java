package be.lutske.leolegacy.entrypoint.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Integration test for the /api/version endpoint.
 *
 * Verifies that:
 * - The endpoint returns HTTP 200
 * - The response content-type is application/json
 * - The response body contains a "version" field matching the configured value
 */
@QuarkusTest
class VersionResourceTest {

    @Test
    void getApiVersionReturns200WithJsonVersion() {
        given()
                .when()
                .get("/api/version")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("version", is(notNullValue()))
                .body("version", is("1.2.3"));
    }

    @Test
    void getApiVersionResponseHasCorrectContentType() {
        given()
                .when()
                .get("/api/version")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }
}
