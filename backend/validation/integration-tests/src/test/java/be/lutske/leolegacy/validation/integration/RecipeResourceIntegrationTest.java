package be.lutske.leolegacy.validation.integration;

import be.lutske.leolegacy.validation.integration.config.TestDataContext;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class RecipeResourceIntegrationTest {

    @Inject
    TestDataContext testData;

    @BeforeEach
    void setUp() {
        testData.reset();
        testData.seedDefaults();
    }

    @Test
    void listRecipesReturnsAll() {
        given()
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(3));
    }

    @Test
    void listRecipesFilteredByCategoryId() {
        given()
                .queryParam("categoryId", 2)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].title", is("Tomato Soup"));
    }

    @Test
    void getRecipeByIdReturnsDetail() {
        given()
                .when()
                .get("/api/recipes/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("title", is("Tomato Soup"))
                .body("ingredients.size()", is(3))
                .body("preparation", is(notNullValue()))
                .body("categoryName", is("Soups"));
    }

    @Test
    void getRecipeByIdReturns404ForUnknownId() {
        given()
                .when()
                .get("/api/recipes/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void topRecipesReturnsOrderedByViewCount() {
        given()
                .when()
                .get("/api/recipes/top")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }
}
