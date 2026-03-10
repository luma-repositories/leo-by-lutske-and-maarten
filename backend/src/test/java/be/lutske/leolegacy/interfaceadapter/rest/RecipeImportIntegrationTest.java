package be.lutske.leolegacy.interfaceadapter.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full end-to-end integration test for recipe import from image.
 *
 * Prerequisites:
 * - {@code podman compose up -d} must be running
 * - Tesseract must be installed ({@code brew install tesseract})
 */
@QuarkusTest
@TestProfile(PostgresIntegrationTestProfile.class)
@Tag("integration")
class RecipeImportIntegrationTest {

    private static final String EXPECTED_TITLE = "Scaloppine alla pizzaiola";
    private static final String EXPECTED_PREPARATION =
            "Si mette in una pentola il pomodoro e l'aglio, i capperi, il sale, l'olio, " +
            "le scaloppine ed infine la mozzarella e l'origano. " +
            "Lasciar asciugare il pomodoro e cuocere la carne. (cott. 1 ora)";

    @Test
    void fullImportFlowUploadHandwrittenImageAndVerifyRecipeIsPersistedInDatabase() {
        var imageFile = new File(
                getClass().getClassLoader().getResource("testdata/test-recipe.jpg").getFile()
        );

        var importResponse = given()
                .multiPart("file", imageFile, "image/jpeg")
                .when()
                .post("/api/recipes/import");

        int statusCode = importResponse.statusCode();
        assertTrue(statusCode == 201 || statusCode == 422,
                "Expected 201 or 422, but got " + statusCode);

        // Always test the confirm flow with correct recipe data
        given()
                .contentType("application/json")
                .body(buildConfirmRequestJson())
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("id", is(greaterThan(0)))
                .body("title", is(EXPECTED_TITLE))
                .body("ingredients.size()", is(7))
                .body("categoryName", is("Geimporteerd"))
                .body("viewCount", is(0));
    }

    @Test
    void uploadRealImageProducesOcrOutputWithoutServerError() {
        var imageFile = new File(
                getClass().getClassLoader().getResource("testdata/test-recipe.jpg").getFile()
        );

        var response = given()
                .multiPart("file", imageFile, "image/jpeg")
                .when()
                .post("/api/recipes/import");

        int statusCode = response.statusCode();
        assertTrue(statusCode == 201 || statusCode == 422,
                "Expected 201 or 422, but got " + statusCode);

        if (statusCode == 422) {
            String rawText = response.jsonPath().getString("rawText");
            assertTrue(rawText != null && !rawText.isBlank(), "OCR should have produced some text");
        }
    }

    @Test
    void importedRecipeAppearsInGeimporteerdCategoryListing() {
        var confirmResponse = given()
                .contentType("application/json")
                .body(buildConfirmRequestJson())
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .extract()
                .response();

        int recipeId = confirmResponse.jsonPath().getInt("id");

        given()
                .queryParam("categoryId", 15)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    private String buildConfirmRequestJson() {
        return """
                {
                    "rawText": "OCR text from handwritten recipe image",
                    "proposedRecipe": {
                        "title": null,
                        "ingredients": null,
                        "preparation": null
                    },
                    "userOverrides": {
                        "title": "%s",
                        "ingredients": [
                            "1 scatola di pomodori",
                            "2 spicchi d'aglio",
                            "un po' di capperi",
                            "un po' di sale",
                            "una mozzarella",
                            "un po' d'olio",
                            "origano"
                        ],
                        "preparation": "%s"
                    }
                }""".formatted(EXPECTED_TITLE, EXPECTED_PREPARATION);
    }
}
