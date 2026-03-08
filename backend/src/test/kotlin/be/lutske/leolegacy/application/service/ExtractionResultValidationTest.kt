package be.lutske.leolegacy.application.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ExtractionResult] validation logic.
 * These do NOT require Quarkus — they test pure domain logic.
 */
class ExtractionResultValidationTest {

    @Test
    fun `complete extraction has no missing fields`() {
        val result = ExtractionResult(
            title = "Chocolate Mousse",
            ingredients = listOf("200 g chocolate", "4 eggs", "50 g sugar"),
            steps = listOf("Melt chocolate", "Separate eggs", "Fold together")
        )

        assertTrue(result.isComplete())
        assertTrue(result.missingFields().isEmpty())
    }

    @Test
    fun `missing title is detected`() {
        val result = ExtractionResult(
            title = null,
            ingredients = listOf("200 g chocolate"),
            steps = listOf("Melt chocolate")
        )

        assertFalse(result.isComplete())
        assertEquals(listOf("title"), result.missingFields())
    }

    @Test
    fun `blank title is treated as missing`() {
        val result = ExtractionResult(
            title = "   ",
            ingredients = listOf("200 g chocolate"),
            steps = listOf("Melt chocolate")
        )

        assertFalse(result.isComplete())
        assertTrue(result.missingFields().contains("title"))
    }

    @Test
    fun `missing ingredients is detected`() {
        val result = ExtractionResult(
            title = "Chocolate Mousse",
            ingredients = null,
            steps = listOf("Melt chocolate")
        )

        assertFalse(result.isComplete())
        assertEquals(listOf("ingredients"), result.missingFields())
    }

    @Test
    fun `empty ingredients list is treated as missing`() {
        val result = ExtractionResult(
            title = "Chocolate Mousse",
            ingredients = emptyList(),
            steps = listOf("Melt chocolate")
        )

        assertFalse(result.isComplete())
        assertTrue(result.missingFields().contains("ingredients"))
    }

    @Test
    fun `missing steps is detected as missing preparation`() {
        val result = ExtractionResult(
            title = "Chocolate Mousse",
            ingredients = listOf("200 g chocolate"),
            steps = null
        )

        assertFalse(result.isComplete())
        assertEquals(listOf("preparation"), result.missingFields())
    }

    @Test
    fun `empty steps list is treated as missing preparation`() {
        val result = ExtractionResult(
            title = "Chocolate Mousse",
            ingredients = listOf("200 g chocolate"),
            steps = emptyList()
        )

        assertFalse(result.isComplete())
        assertTrue(result.missingFields().contains("preparation"))
    }

    @Test
    fun `all fields missing returns three missing fields`() {
        val result = ExtractionResult()

        assertFalse(result.isComplete())
        assertEquals(listOf("title", "ingredients", "preparation"), result.missingFields())
    }

    @Test
    fun `warnings are preserved in result`() {
        val result = ExtractionResult(
            title = "Test",
            ingredients = listOf("item"),
            steps = listOf("step"),
            warnings = listOf("Handwriting partially illegible", "Servings unclear")
        )

        assertTrue(result.isComplete())
        assertEquals(2, result.warnings.size)
        assertEquals("Handwriting partially illegible", result.warnings[0])
    }

    @Test
    fun `provider and model metadata are preserved`() {
        val result = ExtractionResult(
            title = "Test",
            ingredients = listOf("item"),
            steps = listOf("step"),
            provider = "openai",
            model = "gpt-4o"
        )

        assertEquals("openai", result.provider)
        assertEquals("gpt-4o", result.model)
    }

    @Test
    fun `optional fields do not affect completeness`() {
        val result = ExtractionResult(
            title = "Test",
            description = null,
            servings = null,
            ingredients = listOf("item"),
            steps = listOf("step"),
            source = null,
            tags = null
        )

        assertTrue(result.isComplete())
        assertTrue(result.missingFields().isEmpty())
    }

    @Test
    fun `rawModelResponse is preserved`() {
        val rawJson = """{"title":"Test","ingredients":["item"],"steps":["step"]}"""
        val result = ExtractionResult(
            title = "Test",
            ingredients = listOf("item"),
            steps = listOf("step"),
            rawModelResponse = rawJson
        )

        assertEquals(rawJson, result.rawModelResponse)
    }
}
