package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.application.service.ExtractionResult
import be.lutske.leolegacy.application.service.RecipeExtractionService
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.quarkus.test.junit.mockito.InjectMock
import io.restassured.RestAssured.given
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
 * Uses real PostgreSQL database (via podman-compose) and mocked AI extraction.
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
            "1 can of tomatoes",
            "2 cloves of garlic",
            "some capers",
            "some salt",
            "one mozzarella",
            "some oil",
            "oregano"
        )
        private const val EXPECTED_PREPARATION =
            "Place the tomato and garlic, capers, salt, oil, " +
            "the scaloppine and finally the mozzarella and oregano in a pot. " +
            "Let the tomato reduce and cook the meat. (cooking time: 1 hour)"
    }

    @BeforeEach
    fun setup() {
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
     * 1. Upload image → get extraction result (200, never auto-saved)
     * 2. Confirm with user data → recipe saved (201)
     * 3. Verify recipe is persisted
     */
    @Test
    fun `full import flow - upload returns extraction for review then confirm saves`() {
        val imageFile = createTempImageFile()

        // Step 1: Upload returns extraction for review (200, not 201)
        given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(200)
            .body("status", `is`("COMPLETE"))
            .body("proposedRecipe.title", `is`(EXPECTED_TITLE))
            .body("proposedRecipe.ingredients.size()", `is`(7))

        // Step 2: Confirm to actually save
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
            .body("preparation", `is`(notNullValue()))
            .body("categoryName", `is`("Geimporteerd"))
            .body("viewCount", `is`(0))
            .extract()
            .response()

        val confirmedRecipeId = confirmResponse.jsonPath().getInt("id")

        // Step 3: Verify persisted
        given()
            .`when`()
            .get("/api/recipes/$confirmedRecipeId")
            .then()
            .statusCode(200)
            .body("id", `is`(confirmedRecipeId))
            .body("title", `is`(EXPECTED_TITLE))
            .body("ingredients.size()", `is`(7))
            .body("categoryId", `is`(15))
            .body("categoryName", `is`("Geimporteerd"))

        imageFile.delete()
    }

    @Test
    fun `upload image returns extraction without server error`() {
        val imageFile = createTempImageFile()

        given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")
            .then()
            .statusCode(200)
            .body("status", `is`(notNullValue()))
            .body("proposedRecipe", `is`(notNullValue()))

        imageFile.delete()
    }

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
                    "1 can of tomatoes",
                    "2 cloves of garlic",
                    "some capers",
                    "some salt",
                    "one mozzarella",
                    "some oil",
                    "oregano"
                ],
                "preparation": "$EXPECTED_PREPARATION"
            }
        }
        """.trimIndent()
    }

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
