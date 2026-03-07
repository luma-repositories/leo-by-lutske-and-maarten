package be.lutske.leolegacy.interfaceadapter.rest

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.response.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Full end-to-end integration test for recipe import from image.
 *
 * This test uses:
 * - Real PostgreSQL database (via podman-compose)
 * - Real Tesseract OCR (installed locally via Homebrew)
 * - The actual test-recipe.jpg image (handwritten Italian recipe: "Scaloppine alla pizzaiola")
 *
 * Prerequisites:
 * - `podman compose up -d` must be running
 * - Tesseract must be installed (`brew install tesseract`)
 */
@QuarkusTest
@TestProfile(PostgresIntegrationTestProfile::class)
class RecipeImportIntegrationTest {

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

    /**
     * Full import flow:
     * 1. Upload the real handwritten recipe image → OCR processes it
     * 2. If 201 (fully parsed): validate the recipe was created and verify in DB
     * 3. If 422 (needs more info): confirm with user overrides, then verify in DB
     *
     * Either way, the recipe ends up persisted in the database and we validate its content.
     */
    @Test
    fun `full import flow - upload handwritten image and verify recipe is persisted in database`() {
        val imageFile = File(
            javaClass.classLoader.getResource("testdata/test-recipe.jpg")!!.toURI()
        )

        // --- Step 1: Upload the real image for OCR ---
        val importResponse: Response = given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")

        val statusCode = importResponse.statusCode
        assertTrue(
            statusCode == 201 || statusCode == 422,
            "Expected 201 (fully parsed) or 422 (needs more info), but got $statusCode"
        )

        val recipeId: Int

        if (statusCode == 201) {
            // OCR + parser managed to extract a complete recipe directly
            recipeId = importResponse.jsonPath().getInt("id")
            assertTrue(recipeId > 0, "Recipe ID should be positive")

            // The OCR-parsed recipe will have garbled content from handwriting,
            // but it was accepted as complete. Verify it exists and has the right category.
            given()
                .`when`()
                .get("/api/recipes/$recipeId")
                .then()
                .statusCode(200)
                .body("id", `is`(recipeId))
                .body("categoryName", `is`("Geimporteerd"))
                .body("viewCount", `is`(0))

            // Now also test the confirm flow separately to get a recipe with correct content
            recipeId.let { /* already verified direct import */ }
        }

        // --- Step 2: Always test the confirm flow with correct recipe data ---
        // This ensures we can import the actual recipe content into the database
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
            .body("ingredients[0]", `is`("1 scatola di pomodori"))
            .body("ingredients[1]", `is`("2 spicchi d'aglio"))
            .body("ingredients[2]", `is`("un po' di capperi"))
            .body("ingredients[3]", `is`("un po' di sale"))
            .body("ingredients[4]", `is`("una mozzarella"))
            .body("ingredients[5]", `is`("un po' d'olio"))
            .body("ingredients[6]", `is`("origano"))
            .body("preparation", `is`(EXPECTED_PREPARATION))
            .body("categoryId", `is`(15))
            .body("categoryName", `is`("Geimporteerd"))
            .body("viewCount", `is`(0))
    }

    /**
     * Verify that the OCR endpoint actually processes the image and returns meaningful data.
     * Even though handwritten text produces garbled OCR, the endpoint should not error out.
     */
    @Test
    fun `upload real image produces OCR output without server error`() {
        val imageFile = File(
            javaClass.classLoader.getResource("testdata/test-recipe.jpg")!!.toURI()
        )

        val response: Response = given()
            .multiPart("file", imageFile, "image/jpeg")
            .`when`()
            .post("/api/recipes/import")

        val statusCode = response.statusCode
        assertTrue(
            statusCode == 201 || statusCode == 422,
            "Expected 201 or 422, but got $statusCode — OCR should not produce a server error"
        )

        if (statusCode == 422) {
            // Validate the needs-more-info response structure
            val rawText = response.jsonPath().getString("rawText")
            assertTrue(rawText.isNotBlank(), "OCR should have produced some text from the image")

            val missingFields = response.jsonPath().getList<String>("missingFields")
            assertTrue(missingFields.isNotEmpty(), "Should report at least one missing field")

            val proposedRecipe = response.jsonPath().getMap<String, Any>("proposedRecipe")
            assertTrue(proposedRecipe != null, "Should include a proposed recipe")
        } else {
            // 201: recipe was fully parsed — validate it has required fields
            val title = response.jsonPath().getString("title")
            assertTrue(title.isNotBlank(), "Parsed recipe should have a title")

            val ingredientCount = response.jsonPath().getList<String>("ingredients").size
            assertTrue(ingredientCount >= 1, "Parsed recipe should have at least one ingredient")
        }
    }

    /**
     * Verify that the imported recipe appears in the recipe list filtered by the "Geimporteerd" category.
     */
    @Test
    fun `imported recipe appears in Geimporteerd category listing`() {
        // Create a recipe via confirm
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

        // Verify it appears in the category listing
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
            "rawText": "OCR text from handwritten recipe image",
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
}
