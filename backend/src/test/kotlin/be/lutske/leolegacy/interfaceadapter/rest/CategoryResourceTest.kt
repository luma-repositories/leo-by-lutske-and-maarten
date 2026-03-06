package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test

/**
 * Integration test for the /api/categories endpoint.
 *
 * Verifies that:
 * - The endpoint returns HTTP 200
 * - The response is JSON
 * - Categories are returned from the seeded database
 */
@QuarkusTest
class CategoryResourceTest {

    @Test
    fun `GET api-categories returns 200 with JSON list`() {
        given()
            .`when`()
            .get("/api/categories")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `GET api-categories returns categories with expected fields`() {
        given()
            .`when`()
            .get("/api/categories")
            .then()
            .statusCode(200)
            .body("[0].id", `is`(greaterThanOrEqualTo(1)))
            .body("[0].name", `is`(org.hamcrest.Matchers.notNullValue()))
            .body("[0].recipeCount", `is`(greaterThanOrEqualTo(0)))
    }
}
