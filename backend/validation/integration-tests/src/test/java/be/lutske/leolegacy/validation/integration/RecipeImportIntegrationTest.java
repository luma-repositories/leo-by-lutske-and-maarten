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
class RecipeImportIntegrationTest {

    @Inject
    TestDataContext testData;

    @BeforeEach
    void setUp() {
        testData.reset();
        testData.seedDefaults();
    }

    @Test
    void confirmImportCreatesRecipe() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "proposedRecipe": {
                                "title": "Test Import Recipe",
                                "ingredients": ["100 g flour", "2 eggs"],
                                "preparation": "Mix ingredients. Bake at 180C."
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("id", is(greaterThanOrEqualTo(1)))
                .body("title", is("Test Import Recipe"))
                .body("ingredients.size()", is(2))
                .body("preparation", is(notNullValue()));
    }

    @Test
    void confirmImportWithOverridesUsesOverrides() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "proposedRecipe": {
                                "title": "Original Title",
                                "ingredients": ["old ingredient"],
                                "preparation": "old step"
                            },
                            "userOverrides": {
                                "title": "Corrected Title",
                                "ingredients": ["200 g chocolate", "4 eggs"],
                                "preparation": "Corrected instructions."
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("title", is("Corrected Title"))
                .body("ingredients.size()", is(2));
    }

    @Test
    void confirmImportWithoutTitleReturns400() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "proposedRecipe": {
                                "ingredients": ["flour"],
                                "preparation": "bake"
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(400);
    }
}
