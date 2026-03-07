package be.lutske.leolegacy.application.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for RecipeParserService.
 * These do NOT require Quarkus — they test pure parsing logic.
 */
class RecipeParserServiceTest {

    private val parser = RecipeParserService()

    @Test
    fun `parse structured recipe with both headings`() {
        val text = """
            Chocolate Mousse
            
            Ingredients:
            200 g dark chocolate
            4 eggs
            50 g sugar
            
            Instructions:
            Melt the chocolate au bain-marie.
            Separate the eggs.
            Whip the egg whites with sugar until stiff.
            Fold into the chocolate mixture.
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("Chocolate Mousse", result.proposedRecipe.title)
        assertNotNull(result.proposedRecipe.ingredients)
        assertEquals(3, result.proposedRecipe.ingredients!!.size)
        assertTrue(result.proposedRecipe.ingredients!!.any { it.contains("chocolate") })
        assertNotNull(result.proposedRecipe.preparation)
        assertTrue(result.proposedRecipe.preparation!!.contains("Melt"))
        assertTrue(result.missingFields.isEmpty(), "Expected no missing fields but got: ${result.missingFields}")
    }

    @Test
    fun `parse recipe with Dutch headings`() {
        val text = """
            Pompoensoep
            
            Benodigdheden:
            1 pompoen
            2 uien
            1 liter bouillon
            
            Bereiding:
            Snijd de pompoen in stukken.
            Bak de uien aan.
            Voeg de bouillon toe en kook 20 minuten.
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("Pompoensoep", result.proposedRecipe.title)
        assertNotNull(result.proposedRecipe.ingredients)
        assertEquals(3, result.proposedRecipe.ingredients!!.size)
        assertNotNull(result.proposedRecipe.preparation)
        assertTrue(result.proposedRecipe.preparation!!.contains("pompoen"))
        assertTrue(result.missingFields.isEmpty())
    }

    @Test
    fun `parse recipe with ALL CAPS title`() {
        val text = """
            CHOCOLATE MOUSSE
            A classic French dessert
            
            Ingredients:
            200 g chocolate
            4 eggs
            
            Directions:
            Melt and fold.
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("CHOCOLATE MOUSSE", result.proposedRecipe.title)
    }

    @Test
    fun `parse recipe without headings uses heuristics`() {
        val text = """
            Simple Cake
            
            2 cups flour
            1 cup sugar
            3 eggs
            
            Mix all ingredients together.
            Bake at 180 degrees for 30 minutes.
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("Simple Cake", result.proposedRecipe.title)
        assertTrue(result.parseWarnings.any { it.contains("heuristic") })
    }

    @Test
    fun `parse empty text returns all fields missing`() {
        val result = parser.parse("")

        assertTrue(result.missingFields.contains("title"))
        assertTrue(result.missingFields.contains("ingredients"))
        assertTrue(result.missingFields.contains("preparation"))
        assertTrue(result.parseWarnings.any { it.contains("no text") })
    }

    @Test
    fun `parse blank text returns all fields missing`() {
        val result = parser.parse("   \n  \n   ")

        assertTrue(result.missingFields.contains("title"))
        assertTrue(result.missingFields.contains("ingredients"))
        assertTrue(result.missingFields.contains("preparation"))
    }

    @Test
    fun `parse recipe with only ingredients heading`() {
        val text = """
            My Recipe
            
            Ingredients:
            100 g butter
            200 g flour
            Some text that is not an ingredient
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("My Recipe", result.proposedRecipe.title)
        assertNotNull(result.proposedRecipe.ingredients)
    }

    @Test
    fun `parse recipe with only instructions heading`() {
        val text = """
            Quick Snack
            
            Method:
            Take bread.
            Add cheese.
            Toast until golden.
        """.trimIndent()

        val result = parser.parse(text)

        assertEquals("Quick Snack", result.proposedRecipe.title)
        assertNotNull(result.proposedRecipe.preparation)
        assertTrue(result.missingFields.contains("ingredients"))
    }

    @Test
    fun `parse result includes missing fields for incomplete recipe`() {
        val text = """
            Ingredients:
            1 cup rice
            2 cups water
        """.trimIndent()

        val result = parser.parse(text)

        assertTrue(result.missingFields.contains("preparation"))
    }

    @Test
    fun `parse handles various instruction heading synonyms`() {
        for (heading in listOf("Instructions:", "Directions:", "Method:", "Steps:", "Preparation:", "Bereiding:", "Werkwijze:")) {
            val text = """
                Test Recipe
                
                Ingredients:
                1 item
                
                $heading
                Do something.
            """.trimIndent()

            val result = parser.parse(text)
            assertNotNull(result.proposedRecipe.preparation, "Failed for heading: $heading")
        }
    }
}
