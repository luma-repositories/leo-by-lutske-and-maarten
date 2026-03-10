package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.service.OcrService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the recipe import endpoints.
 * OcrService is mocked since Tesseract is not available in test environment.
 */
@QuarkusTest
class RecipeImportResourceTest {

    OcrService ocrService;

    @BeforeEach
    void setup() {
        ocrService = mock(OcrService.class);
        when(ocrService.extractText(any())).thenReturn("""
                Chocolate Mousse
                
                Ingredients:
                200 g dark chocolate
                4 eggs
                50 g sugar
                
                Instructions:
                Melt the chocolate au bain-marie.
                Separate the eggs.
                Whip the egg whites with sugar.
                Fold into the chocolate.""");
        QuarkusMock.installMockForType(ocrService, OcrService.class);
    }

    @Test
    void postImportWithValidImageCreatesRecipeWhenFullyParsed() throws IOException {
        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(201)
                .body("id", is(greaterThanOrEqualTo(1)))
                .body("title", is("Chocolate Mousse"))
                .body("ingredients.size()", is(3))
                .body("preparation", is(notNullValue()))
                .body("categoryName", is("Geimporteerd"));

        tempFile.delete();
    }

    @Test
    void postImportReturns422WhenRecipeIsIncomplete() throws IOException {
        when(ocrService.extractText(any())).thenReturn("""
                Some random text without clear structure
                that doesn't look like a recipe at all
                just some words and sentences""");
        QuarkusMock.installMockForType(ocrService, OcrService.class);

        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(422)
                .body("status", is("NEEDS_MORE_INFO"))
                .body("rawText", is(notNullValue()))
                .body("proposedRecipe", is(notNullValue()))
                .body("missingFields.size()", greaterThanOrEqualTo(1));

        tempFile.delete();
    }

    @Test
    void postConfirmCreatesRecipeFromUserProvidedData() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "rawText": "some ocr text",
                            "proposedRecipe": {
                                "title": "Test Recipe",
                                "ingredients": ["100 g flour", "2 eggs"],
                                "preparation": "Mix and bake."
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("id", is(greaterThanOrEqualTo(1)))
                .body("title", is("Test Recipe"))
                .body("ingredients.size()", is(2))
                .body("preparation", is("Mix and bake."));
    }

    @Test
    void postConfirmWithOverridesUsesOverrideValues() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "rawText": "some ocr text",
                            "proposedRecipe": {
                                "title": "Original Title",
                                "ingredients": ["old ingredient"],
                                "preparation": "old steps"
                            },
                            "userOverrides": {
                                "title": "Corrected Title",
                                "ingredients": ["200 g chocolate", "4 eggs", "50 g sugar"],
                                "preparation": "Corrected preparation steps.",
                                "notes": "User added this note"
                            }
                        }""")
                .when()
                .post("/api/recipes/import/confirm")
                .then()
                .statusCode(201)
                .body("title", is("Corrected Title"))
                .body("ingredients.size()", is(3));
    }

    @Test
    void postConfirmWithoutTitleReturns400() {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "rawText": "some text",
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
    void postImportWithEmptyOcrTextReturns422() throws IOException {
        when(ocrService.extractText(any())).thenReturn("");
        QuarkusMock.installMockForType(ocrService, OcrService.class);

        File tempFile = createTempImageFile();

        given()
                .multiPart("file", tempFile, "image/png")
                .when()
                .post("/api/recipes/import")
                .then()
                .statusCode(422)
                .body("status", is("NEEDS_MORE_INFO"))
                .body("missingFields.size()", is(3));

        tempFile.delete();
    }

    /**
     * Creates a minimal temporary PNG file for testing.
     * The actual image content doesn't matter since OcrService is mocked.
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
