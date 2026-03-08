package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.ExtractionResult
import be.lutske.leolegacy.application.service.RecipeExtractionException
import be.lutske.leolegacy.application.service.RecipeExtractionService
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
import org.mockito.kotlin.eq
import java.io.File

/**
 * Integration tests for the recipe import endpoints.
 * RecipeExtractionService is mocked since real LLM calls are not available in test environment.
 */
@QuarkusTest
class RecipeImportResourceTest {

    @InjectMock
    lateinit var extractionService: RecipeExtractionService

    @BeforeEach
    fun setup() {
        // Default mock: return a complete extraction result
        `when`(extractionService.extractRecipeFromImage(any(), any())).thenReturn(
            ExtractionResult(
                title = "Chocolate Mousse",
                description = "A classic French dessert",
                servings = "4 servings",
                ingredients = listOf("200 g dark chocolate", "4 eggs", "50 g sugar"),
                steps = listOf(
                    "Melt the chocolate au bain-marie.",
                    "Separate the eggs.",
                    "Whip the egg whites with sugar.",
                    "Fold into the chocolate."
                ),
                warnings = emptyList(),
                rawModelResponse = """{"title":"Chocolate Mousse"}""",
                provider = "openai",
                model = "gpt-4o"
            )
        )
    }

    @Test
    fun `POST import with valid image creates recipe when fully extracted`() {
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
    fun `POST import returns 422 when extraction is incomplete`() {
        `when`(extractionService.extractRecipeFromImage(any(), any())).thenReturn(
            ExtractionResult(
                title = "Some Recipe",
                ingredients = null,
                steps = null,
                warnings = listOf("Could not read ingredients from image"),
                rawModelResponse = """{"title":"Some Recipe","warnings":["Could not read ingredients"]}""",
                provider = "openai",
                model = "gpt-4o"
            )
        )

        val tempFile = createTempImageFile()

        given()
            .multiPart("file", tempFile, "image/png")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(422)
            .body("status", `is`("NEEDS_MORE_INFO"))
            .body("rawModelResponse", `is`(notNullValue()))
            .body("proposedRecipe", `is`(notNullValue()))
            .body("missingFields.size()", greaterThanOrEqualTo(1))
            .body("warnings.size()", greaterThanOrEqualTo(1))

        tempFile.delete()
    }

    @Test
    fun `POST import returns 502 when extraction service fails`() {
        `when`(extractionService.extractRecipeFromImage(any(), any())).thenThrow(
            RecipeExtractionException("Provider unavailable: connection refused")
        )

        val tempFile = createTempImageFile()

        given()
            .multiPart("file", tempFile, "image/png")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(502)
            .body("error", `is`(notNullValue()))

        tempFile.delete()
    }

    @Test
    fun `POST confirm creates recipe from user-provided data`() {
        given()
            .contentType("application/json")
            .body(
                """
                {
                    "rawModelResponse": "some model output",
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
                    "rawModelResponse": "some model output",
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
                    "rawModelResponse": "some text",
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
    fun `POST import with empty extraction returns 422`() {
        `when`(extractionService.extractRecipeFromImage(any(), any())).thenReturn(
            ExtractionResult(
                warnings = listOf("Image does not appear to contain a recipe"),
                rawModelResponse = """{"title":null,"warnings":["Image does not appear to contain a recipe"]}""",
                provider = "openai",
                model = "gpt-4o"
            )
        )

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
     * The actual image content doesn't matter since RecipeExtractionService is mocked.
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
