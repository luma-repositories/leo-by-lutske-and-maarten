package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Integration test verifying the static homepage is served by Quarkus.
 *
 * Verifies that:
 * - GET / returns HTTP 200
 * - The response contains expected HTML content from the legacy site
 * - jQuery script tag is present (for version fetching)
 * - The version placeholder element is present
 */
@QuarkusTest
class HomePageAvailabilityTest {

    @Test
    void getRootReturns200WithHtmlContent() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(containsString("leo-legacy.be"));
    }

    @Test
    void getRootContainsVersionPlaceholderElement() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(containsString("id=\"appVersion\""));
    }

    @Test
    void getRootIncludesJqueryScript() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(containsString("jquery"));
    }

    @Test
    void getIndexHtmlReturns200() {
        given()
                .when()
                .get("/index.html")
                .then()
                .statusCode(200)
                .body(containsString("Welkom op leo-legacy.be"));
    }
}
