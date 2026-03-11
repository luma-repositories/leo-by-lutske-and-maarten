package be.lutske.leolegacy.entrypoint.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

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
    void getApiCategoriesReturns200WithJsonList() {
        given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getApiCategoriesReturnsCategoriesWithExpectedFields() {
        given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("[0].id", is(greaterThanOrEqualTo(1)))
                .body("[0].name", is(notNullValue()))
                .body("[0].recipeCount", is(greaterThanOrEqualTo(0)));
    }
}
