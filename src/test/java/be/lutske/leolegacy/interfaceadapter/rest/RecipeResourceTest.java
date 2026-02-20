package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class RecipeResourceTest {

    @Test
    void shouldReturnRecipeList() {
        given()
                .when().get("/api/recipes")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue())
                .body("[0].category", notNullValue());
    }

    @Test
    void shouldReturnRecipeDetail() {
        given()
                .when().get("/api/recipes/1")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", notNullValue())
                .body("ingredients", notNullValue())
                .body("preparation", notNullValue());
    }
}
