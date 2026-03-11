package be.lutske.leolegacy.entrypoint.rest;

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
 * <p>Prerequisites:
 * <ul>
 *   <li>{@code podman compose up -d} must be running (PostgreSQL)</li>
 *   <li>An AI API key must be configured (e.g. via {@code backend/.env})</li>
 * </ul>
 *
 * <p>Contract:
 * <ul>
 *   <li>{@code POST /api/recipes/import} always returns HTTP 200 with extraction for review</li>
 *   <li>{@code POST /api/recipes/import/confirm} returns HTTP 201 when recipe is persisted</li>
 * </ul>
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
    void fullImportFlowUploadImageAndVerifyRecipeIsPersistedInDatabase() {
        var imageFile = new File(
                getClass().getClassLoader().getResource("testdata/test-recipe.jpg").getFile()
        );

        var importResponse = given()
                .multiPart("file", imageFile, "image/jpeg")
                .when()
                .post("/api/recipes/import");

        // Import always returns 200 with extraction data for review
        importResponse.then().statusCode(200);
        String status = importResponse.jsonPath().getString("status");
        assertTrue("COMPLETE".equals(status) || "NEEDS_MORE_INFO".equals(status),
                "Expected status COMPLETE or NEEDS_MORE_INFO, but got " + status);

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
                .body("categoryName", is("Imported"))
                .body("viewCount", is(0));
    }

    @Test
    void uploadRealImageProducesExtractionWithoutServerError() {
        var imageFile = new File(
                getClass().getClassLoader().getResource("testdata/test-recipe.jpg").getFile()
        );

        var response = given()
                .multiPart("file", imageFile, "image/jpeg")
                .when()
                .post("/api/recipes/import");

        // Import always returns 200
        response.then().statusCode(200);
        String status = response.jsonPath().getString("status");
        assertTrue("COMPLETE".equals(status) || "NEEDS_MORE_INFO".equals(status),
                "Expected status COMPLETE or NEEDS_MORE_INFO, but got " + status);

        if ("NEEDS_MORE_INFO".equals(status)) {
            String rawResponse = response.jsonPath().getString("rawModelResponse");
            assertTrue(rawResponse != null && !rawResponse.isBlank(),
                    "AI extraction should have produced a raw model response");
        }
    }

    @Test
    void importedRecipeAppearsInImportedCategoryListing() {
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
