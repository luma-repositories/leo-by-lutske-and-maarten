package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.OcrService
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.io.File

/**
 * Integration tests for the recipe import endpoints.
 * OcrService is mocked since Tesseract is not available in test environment.
 */
@QuarkusTest
class RecipeImportResourceTest {

    @InjectMock
    lateinit var ocrService: OcrService

    @BeforeEach
    fun setup() {
        // Default mock: return a well-structured recipe text
        `when`(ocrService.extractText(any())).thenReturn(
            """
            Chocolate Mousse
            
            Ingredients:
            200 g dark chocolate
            4 eggs
            50 g sugar
            
            Instructions:
            Melt the chocolate au bain-marie.
            Separate the eggs.
            Whip the egg whites with sugar.
            Fold into the chocolate.
            """.trimIndent()
        )
    }

    @Test
    fun `POST import with valid image creates recipe when fully parsed`() {
        val tempFile = createTempImageFile()

        given()
            .multiPart("file", tempFile, "image/png")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(201)
            .body("id", `is`(greaterThanOrEqualTo(1)))
            .body("title", `is`("Chocolate Mousse"))
            .body("ingredients.size()", `is`(3))
            .body("preparation", `is`(notNullValue()))
            .body("categoryName", `is`("Geimporteerd"))

        tempFile.delete()
    }

    @Test
    fun `POST import returns 422 when recipe is incomplete`() {
        `when`(ocrService.extractText(any())).thenReturn(
            """
            Some random text without clear structure
            that doesn't look like a recipe at all
            just some words and sentences
            """.trimIndent()
        )

        val tempFile = createTempImageFile()

        given()
            .multiPart("file", tempFile, "image/png")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(422)
            .body("status", `is`("NEEDS_MORE_INFO"))
            .body("rawText", `is`(notNullValue()))
            .body("proposedRecipe", `is`(notNullValue()))
            .body("missingFields.size()", greaterThanOrEqualTo(1))

        tempFile.delete()
    }

    @Test
    fun `POST confirm creates recipe from user-provided data`() {
        given()
            .contentType("application/json")
            .body(
                """
                {
                    "rawText": "some ocr text",
                    "proposedRecipe": {
                        "title": "Test Recipe",
                        "ingredients": ["100 g flour", "2 eggs"],
                        "preparation": "Mix and bake."
                    }
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/recipes/import/confirm")
            .then()
            .statusCode(201)
            .body("id", `is`(greaterThanOrEqualTo(1)))
            .body("title", `is`("Test Recipe"))
            .body("ingredients.size()", `is`(2))
            .body("preparation", `is`("Mix and bake."))
    }

    @Test
    fun `POST confirm with overrides uses override values`() {
        given()
            .contentType("application/json")
            .body(
                """
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
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/recipes/import/confirm")
            .then()
            .statusCode(201)
            .body("title", `is`("Corrected Title"))
            .body("ingredients.size()", `is`(3))
    }

    @Test
    fun `POST confirm without title returns 400`() {
        given()
            .contentType("application/json")
            .body(
                """
                {
                    "rawText": "some text",
                    "proposedRecipe": {
                        "ingredients": ["flour"],
                        "preparation": "bake"
                    }
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/recipes/import/confirm")
            .then()
            .statusCode(400)
    }

    @Test
    fun `POST import with empty OCR text returns 422`() {
        `when`(ocrService.extractText(any())).thenReturn("")

        val tempFile = createTempImageFile()

        given()
            .multiPart("file", tempFile, "image/png")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(422)
            .body("status", `is`("NEEDS_MORE_INFO"))
            .body("missingFields.size()", `is`(3))

        tempFile.delete()
    }

    /**
     * Creates a minimal temporary PNG file for testing.
     * The actual image content doesn't matter since OcrService is mocked.
     */
    private fun createTempImageFile(): File {
        val tempFile = File.createTempFile("test-recipe", ".png")
        // Write minimal PNG header bytes so it's a valid-ish file
        tempFile.writeBytes(
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, // IHDR length
                0x49, 0x48, 0x44, 0x52  // IHDR type
            )
        )
        return tempFile
    }
}
