package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test

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
    fun `GET root returns 200 with HTML content`() {
        given()
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(containsString("leo-legacy.be"))
    }

    @Test
    fun `GET root contains version placeholder element`() {
        given()
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(containsString("id=\"appVersion\""))
    }

    @Test
    fun `GET root includes jQuery script`() {
        given()
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(containsString("jquery"))
    }

    @Test
    fun `GET index-html returns 200`() {
        given()
            .`when`()
            .get("/index.html")
            .then()
            .statusCode(200)
            .body(containsString("Welkom op leo-legacy.be"))
    }
}
