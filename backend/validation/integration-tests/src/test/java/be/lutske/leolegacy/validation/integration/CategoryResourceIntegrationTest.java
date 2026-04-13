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
class CategoryResourceIntegrationTest {

    @Inject
    TestDataContext testData;

    @BeforeEach
    void setUp() {
        testData.reset();
        testData.seedDefaults();
    }

    @Test
    void listCategoriesReturnsSeededCategories() {
        given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(5));
    }

    @Test
    void categoriesHaveExpectedFields() {
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
