package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for the /api/recipes endpoint.
 *
 * Verifies list, detail, filter-by-category, and top recipes.
 */
@QuarkusTest
class RecipeResourceTest {

    @Test
    void getApiRecipesReturns200WithJsonList() {
        given()
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getApiRecipesReturnsRecipesWithExpectedFields() {
        given()
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("[0].id", is(greaterThanOrEqualTo(1)))
                .body("[0].title", is(notNullValue()))
                .body("[0].categoryName", is(notNullValue()));
    }

    @Test
    void getApiRecipesWithCategoryIdFilterReturnsFilteredResults() {
        given()
                .queryParam("categoryId", 1)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getApiRecipesTopReturnsTopRecipes() {
        given()
                .when()
                .get("/api/recipes/top")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    void getApiRecipesIdReturnsRecipeDetail() {
        given()
                .when()
                .get("/api/recipes/1")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("id", is(1))
                .body("title", is(notNullValue()))
                .body("ingredients.size()", greaterThanOrEqualTo(1))
                .body("preparation", is(notNullValue()))
                .body("categoryName", is(notNullValue()));
    }

    @Test
    void getApiRecipesIdReturns404ForUnknownId() {
        given()
                .when()
                .get("/api/recipes/999999")
                .then()
                .statusCode(404);
    }
}
