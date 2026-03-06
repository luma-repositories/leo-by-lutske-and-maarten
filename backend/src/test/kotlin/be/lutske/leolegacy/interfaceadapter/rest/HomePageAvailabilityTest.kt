package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test

/**
 * Integration test verifying the core API endpoints are available and healthy.
 *
 * Replaces the old static-site availability tests now that the frontend
 * is a separate React application and the legacy HTML files have been removed.
 */
@QuarkusTest
class HomePageAvailabilityTest {

    @Test
    fun `GET api-version returns 200`() {
        given()
            .`when`()
            .get("/api/version")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("version", `is`(notNullValue()))
    }

    @Test
    fun `GET api-categories returns 200 with data`() {
        given()
            .`when`()
            .get("/api/categories")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `GET api-recipes returns 200 with data`() {
        given()
            .`when`()
            .get("/api/recipes")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `GET api-recipes-top returns 200`() {
        given()
            .`when`()
            .get("/api/recipes/top")
            .then()
            .statusCode(200)
            .contentType("application/json")
    }
}
