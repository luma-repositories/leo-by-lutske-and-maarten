package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

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
    fun `GET api-version returns 200 with JSON version`() {
        given()
            .`when`()
            .get("/api/version")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("version", `is`(notNullValue()))
            .body("version", `is`("1.2.3"))
    }

    @Test
    fun `GET api-version response has correct content type`() {
        given()
            .`when`()
            .get("/api/version")
            .then()
            .statusCode(200)
            .contentType("application/json")
    }
}
