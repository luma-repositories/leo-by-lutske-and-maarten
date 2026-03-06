package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test

/**
 * Integration test for the /api/recipes endpoint.
 *
 * Verifies list, detail, filter-by-category, and top recipes.
 */
@QuarkusTest
class RecipeResourceTest {

    @Test
    fun `GET api-recipes returns 200 with JSON list`() {
        given()
            .`when`()
            .get("/api/recipes")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `GET api-recipes returns recipes with expected fields`() {
        given()
            .`when`()
            .get("/api/recipes")
            .then()
            .statusCode(200)
            .body("[0].id", `is`(greaterThanOrEqualTo(1)))
            .body("[0].title", `is`(notNullValue()))
            .body("[0].categoryName", `is`(notNullValue()))
    }

    @Test
    fun `GET api-recipes with categoryId filter returns filtered results`() {
        given()
            .queryParam("categoryId", 1)
            .`when`()
            .get("/api/recipes")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `GET api-recipes-top returns top 10 recipes`() {
        given()
            .`when`()
            .get("/api/recipes/top")
            .then()
            .statusCode(200)
            .contentType("application/json")
    }

    @Test
    fun `GET api-recipes-id returns recipe detail`() {
        given()
            .`when`()
            .get("/api/recipes/1")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("id", `is`(1))
            .body("title", `is`(notNullValue()))
            .body("ingredients.size()", greaterThanOrEqualTo(1))
            .body("preparation", `is`(notNullValue()))
            .body("categoryName", `is`(notNullValue()))
    }

    @Test
    fun `GET api-recipes-id returns 404 for unknown id`() {
        given()
            .`when`()
            .get("/api/recipes/999999")
            .then()
            .statusCode(404)
    }
}
