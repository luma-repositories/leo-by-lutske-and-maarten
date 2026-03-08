package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.ExtractionResult
import be.lutske.leolegacy.application.service.RecipeExtractionService
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured.given
import io.restassured.response.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.io.File

/**
 * Full end-to-end integration test for recipe import from image.
 *
 * This test uses:
 * - Real PostgreSQL database (via podman-compose)
 * - Mocked AI extraction service (LLM calls are not made in tests)
 *
 * Prerequisites:
 * - `podman compose up -d` must be running
 */
@QuarkusTest
@TestProfile(PostgresIntegrationTestProfile::class)
class RecipeImportIntegrationTest {

    @InjectMock
    lateinit var extractionService: RecipeExtractionService

    companion object {
        private const val EXPECTED_TITLE = "Scaloppine alla pizzaiola"
        private val EXPECTED_INGREDIENTS = listOf(
            "1 scatola di pomodori",
            "2 spicchi d'aglio",
            "un po' di capperi",
            "un po' di sale",
            "una mozzarella",
            "un po' d'olio",
            "origano"
        )
        private const val EXPECTED_PREPARATION =
            "Si mette in una pentola il pomodoro e l'aglio, i capperi, il sale, l'olio, " +
            "le scaloppine ed infine la mozzarella e l'origano. " +
            "Lasciar asciugare il pomodoro e cuocere la carne. (cott. 1 ora)"
    }

    @BeforeEach
    fun setup() {
        // Mock the extraction service to return a partial result (simulating real LLM behavior)
        `when`(extractionService.extractRecipeFromImage(any(), any())).thenReturn(
            ExtractionResult(
                title = EXPECTED_TITLE,
                ingredients = EXPECTED_INGREDIENTS,
                steps = listOf(EXPECTED_PREPARATION),
                warnings = listOf("Handwritten text — some words may be inaccurate"),
                rawModelResponse = """{"title":"$EXPECTED_TITLE"}""",
                provider = "openai",
                model = "gpt-4o"
            )
        )
    }

    /**
     * Full import flow:
     * 1. Upload an image → LLM extraction processes it (mocked)
     * 2. If 201 (fully extracted): validate the recipe was created and verify in DB
     * 3. Also test the confirm flow with user overrides, then verify in DB
     */
    @Test
    fun `full import flow - upload image and verify recipe is persisted in database`() {
        val imageFile = createTempImageFile()

        // --- Step 1: Upload the image for LLM extraction ---
        val importResponse: Response = given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")

        val statusCode = importResponse.statusCode
        assertTrue(
            statusCode == 201 || statusCode == 422,
            "Expected 201 (fully extracted) or 422 (needs more info), but got $statusCode"
        )

        if (statusCode == 201) {
            val recipeId = importResponse.jsonPath().getInt("id")
            assertTrue(recipeId > 0, "Recipe ID should be positive")

            given()
                .`when`()
                .get("/api/recipes/$recipeId")
                .then()
                .statusCode(200)
                .body("id", `is`(recipeId))
                .body("categoryName", `is`("Geimporteerd"))
                .body("viewCount", `is`(0))
        }

        // --- Step 2: Always test the confirm flow with correct recipe data ---
        val confirmResponse = given()
            .contentType("application/json")
            .body(buildConfirmRequestJson())
            .`when`()
            .post("/api/recipes/import/confirm")
            .then()
            .statusCode(201)
            .body("id", `is`(greaterThan(0)))
            .body("title", `is`(EXPECTED_TITLE))
            .body("ingredients.size()", `is`(7))
            .body("ingredients[0]", `is`("1 scatola di pomodori"))
            .body("ingredients[1]", `is`("2 spicchi d'aglio"))
            .body("ingredients[2]", `is`("un po' di capperi"))
            .body("ingredients[3]", `is`("un po' di sale"))
            .body("ingredients[4]", `is`("una mozzarella"))
            .body("ingredients[5]", `is`("un po' d'olio"))
            .body("ingredients[6]", `is`("origano"))
            .body("preparation", `is`(notNullValue()))
            .body("categoryName", `is`("Geimporteerd"))
            .body("viewCount", `is`(0))
            .extract()
            .response()

        val confirmedRecipeId = confirmResponse.jsonPath().getInt("id")

        // --- Step 3: Verify the confirmed recipe is persisted in the database ---
        given()
            .`when`()
            .get("/api/recipes/$confirmedRecipeId")
            .then()
            .statusCode(200)
            .body("id", `is`(confirmedRecipeId))
            .body("title", `is`(EXPECTED_TITLE))
            .body("ingredients.size()", `is`(7))
            .body("preparation", `is`(EXPECTED_PREPARATION))
            .body("categoryId", `is`(15))
            .body("categoryName", `is`("Geimporteerd"))
            .body("viewCount", `is`(0))

        imageFile.delete()
    }

    /**
     * Verify that the import endpoint processes the image and returns meaningful data.
     */
    @Test
    fun `upload image produces extraction output without server error`() {
        val imageFile = createTempImageFile()

        val response: Response = given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")

        val statusCode = response.statusCode
        assertTrue(
            statusCode == 201 || statusCode == 422,
            "Expected 201 or 422, but got $statusCode — extraction should not produce a server error"
        )

        if (statusCode == 422) {
            val rawResponse = response.jsonPath().getString("rawModelResponse")
            assertTrue(rawResponse != null && rawResponse.isNotBlank(), "Should include raw model response")

            val missingFields = response.jsonPath().getList<String>("missingFields")
            assertTrue(missingFields != null, "Should report missing fields")

            val proposedRecipe = response.jsonPath().getMap<String, Any>("proposedRecipe")
            assertTrue(proposedRecipe != null, "Should include a proposed recipe")
        } else {
            val title = response.jsonPath().getString("title")
            assertTrue(title.isNotBlank(), "Extracted recipe should have a title")

            val ingredientCount = response.jsonPath().getList<String>("ingredients").size
            assertTrue(ingredientCount >= 1, "Extracted recipe should have at least one ingredient")
        }

        imageFile.delete()
    }

    /**
     * Verify that the imported recipe appears in the recipe list filtered by the "Geimporteerd" category.
     */
    @Test
    fun `imported recipe appears in Geimporteerd category listing`() {
        val confirmResponse = given()
            .contentType("application/json")
            .body(buildConfirmRequestJson())
            .`when`()
            .post("/api/recipes/import/confirm")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val recipeId = confirmResponse.jsonPath().getInt("id")
        val recipeTitle = confirmResponse.jsonPath().getString("title")

        val recipesInCategory = given()
            .queryParam("categoryId", 15)
            .`when`()
            .get("/api/recipes")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .extract()
            .jsonPath()
            .getList<Map<String, Any>>("")

        val found = recipesInCategory.any { it["id"] == recipeId && it["title"] == recipeTitle }
        assertTrue(found, "Imported recipe '$recipeTitle' (id=$recipeId) should appear in Geimporteerd category")
    }

    private fun buildConfirmRequestJson(): String {
        return """
        {
            "rawModelResponse": "LLM extraction output from handwritten recipe image",
            "proposedRecipe": {
                "title": null,
                "ingredients": null,
                "preparation": null
            },
            "userOverrides": {
                "title": "$EXPECTED_TITLE",
                "ingredients": [
                    "1 scatola di pomodori",
                    "2 spicchi d'aglio",
                    "un po' di capperi",
                    "un po' di sale",
                    "una mozzarella",
                    "un po' d'olio",
                    "origano"
                ],
                "preparation": "$EXPECTED_PREPARATION"
            }
        }
        """.trimIndent()
    }

    /**
     * Creates a minimal temporary PNG file for testing.
     * The actual image content doesn't matter since RecipeExtractionService is mocked.
     */
    private fun createTempImageFile(): File {
        val tempFile = File.createTempFile("test-recipe", ".png")
        tempFile.writeBytes(
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52
            )
        )
        return tempFile
    }
}
