package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.service.ExtractionResult;
import be.lutske.leolegacy.infrastructure.ai.LangChain4jRecipeExtractionService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the recipe import endpoints.
 * RecipeExtractionService is mocked since no real AI provider is available in test environment.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@code POST /api/recipes/import} always returns HTTP 200 with extraction for user review</li>
 *   <li>{@code POST /api/recipes/import/confirm} returns HTTP 201 when recipe is persisted</li>
 * </ul>
 */
@QuarkusTest
class RecipeImportResourceTest {

    LangChain4jRecipeExtractionService extractionService;

    @BeforeEach
    void setup() {
        extractionService = mock(LangChain4jRecipeExtractionService.class);
        when(extractionService.extractRecipeFromImage(any(), anyString())).thenReturn(
                new ExtractionResult(
                        "Chocolate Mousse",
                        "A rich chocolate dessert",
                        "4 servings",
                        List.of("200 g dark chocolate", "4 eggs", "50 g sugar"),
                        List.of("Melt the chocolate au bain-marie.",
                                "Separate the eggs.",
                                "Whip the egg whites with sugar.",
                                "Fold into the chocolate."),
                        null, null,
                        List.of(),
                        "{\"title\":\"Chocolate Mousse\"}",
                        "openai", "gpt-4o"
                ));
        QuarkusMock.installMockForType(extractionService, LangChain4jRecipeExtractionService.class);
    }

    @Test
    void postImportWithValidImageReturns200WithCompleteExtraction() throws IOException {
        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(200)
                .body("status", is("COMPLETE"))
                .body("proposedRecipe.title", is("Chocolate Mousse"))
                .body("proposedRecipe.ingredients.size()", is(3))
                .body("proposedRecipe.ingredients[0]", is("7.1 oz (200 g) dark chocolate"))
                .body("proposedRecipe.convertedIngredients[0].originalQuantity", is("200 g"))
                .body("proposedRecipe.convertedIngredients[0].convertedQuantity", is("7.1 oz"))
                .body("proposedRecipe.convertedIngredients[0].ingredientName", is("dark chocolate"))
                .body("proposedRecipe.preparation", is(notNullValue()))
                .body("rawModelResponse", is(notNullValue()))
                .body("missingFields.size()", is(0));

        tempFile.delete();
    }

    @Test
    void postImportReturns200WithNeedsMoreInfoWhenExtractionIsIncomplete() throws IOException {
        when(extractionService.extractRecipeFromImage(any(), anyString())).thenReturn(
                new ExtractionResult(
                        null, null, null, null, null, null, null,
                        List.of("Image does not appear to contain a recipe"),
                        "{}", "openai", "gpt-4o"
                ));
        QuarkusMock.installMockForType(extractionService, LangChain4jRecipeExtractionService.class);

        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(200)
                .body("status", is("NEEDS_MORE_INFO"))
                .body("rawModelResponse", is(notNullValue()))
                .body("proposedRecipe", is(notNullValue()))
                .body("missingFields.size()", greaterThanOrEqualTo(1))
                .body("warnings.size()", greaterThanOrEqualTo(1));

        tempFile.delete();
    }

    @Test
    void postConfirmCreatesRecipeFromUserProvidedData() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "proposedRecipe": {
                                "title": "Test Recipe",
                                "ingredients": ["100 g flour", "2 eggs"],
                                "preparation": "Mix ingredients.\\nBake at 180C."
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("id", is(greaterThanOrEqualTo(1)))
                .body("title", is("Test Recipe"))
                .body("ingredients.size()", is(2))
                .body("ingredients[0]", is("3.5 oz (100 g) flour"))
                .body("preparation", is(notNullValue()));
    }

    @Test
    void postConfirmWithOverridesUsesOverrideValues() {
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
                                "ingredients": ["200 g chocolate", "4 eggs", "50 g sugar"],
                                "preparation": "Corrected step 1.\\nCorrected step 2.",
                                "notes": "User added this note"
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("title", is("Corrected Title"))
                .body("ingredients.size()", is(3))
                .body("ingredients[0]", is("7.1 oz (200 g) chocolate"));
    }

    @Test
    void postConfirmWithNotesAppendsEnglishNotesPrefix() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "proposedRecipe": {
                                "title": "Test Recipe",
                                "ingredients": ["100 g flour"],
                                "preparation": "Mix well."
                            },
                            "userOverrides": {
                                "notes": "Extra tip from user"
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("preparation", is("Mix well.\n\nNotes: Extra tip from user"));
    }

    @Test
    void postConfirmWithoutTitleReturns400() {
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

    @Test
    void postImportWithEmptyExtractionReturns200WithNeedsMoreInfo() throws IOException {
        when(extractionService.extractRecipeFromImage(any(), anyString())).thenReturn(
                new ExtractionResult());
        QuarkusMock.installMockForType(extractionService, LangChain4jRecipeExtractionService.class);

        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(200)
                .body("status", is("NEEDS_MORE_INFO"))
                .body("missingFields.size()", is(3));

        tempFile.delete();
    }

    /**
     * Creates a minimal temporary PNG file for testing.
     * The actual image content doesn't matter since RecipeExtractionService is mocked.
     */
    private File createTempImageFile() throws IOException {
        File tempFile = File.createTempFile("test-recipe", ".png");
        try (var fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                    0x00, 0x00, 0x00, 0x0D, // IHDR length
                    0x49, 0x48, 0x44, 0x52  // IHDR type
            });
        }
        return tempFile;
    }
}
