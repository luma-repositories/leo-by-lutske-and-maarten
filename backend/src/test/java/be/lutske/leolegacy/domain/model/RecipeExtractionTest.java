package be.lutske.leolegacy.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RecipeExtraction} validation logic.
 * Pure domain logic — no framework needed.
 */
class RecipeExtractionTest {

    @Test
    void completeExtractionHasNoMissingFields() {
        var result = new RecipeExtraction(
                "Chocolate Mousse", null, null,
                List.of("200 g chocolate", "4 eggs", "50 g sugar"),
                List.of("Melt chocolate", "Separate eggs", "Fold together"),
                null, null, List.of(), null, null, null);

        assertTrue(result.isComplete());
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void missingTitleIsDetected() {
        var result = new RecipeExtraction(
                null, null, null,
                List.of("200 g chocolate"),
                List.of("Melt chocolate"),
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertEquals(List.of("title"), result.missingFields());
    }

    @Test
    void blankTitleIsTreatedAsMissing() {
        var result = new RecipeExtraction(
                "   ", null, null,
                List.of("200 g chocolate"),
                List.of("Melt chocolate"),
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertTrue(result.missingFields().contains("title"));
    }

    @Test
    void missingIngredientsIsDetected() {
        var result = new RecipeExtraction(
                "Chocolate Mousse", null, null,
                null,
                List.of("Melt chocolate"),
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertEquals(List.of("ingredients"), result.missingFields());
    }

    @Test
    void emptyIngredientsListIsTreatedAsMissing() {
        var result = new RecipeExtraction(
                "Chocolate Mousse", null, null,
                List.of(),
                List.of("Melt chocolate"),
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertTrue(result.missingFields().contains("ingredients"));
    }

    @Test
    void missingStepsIsDetectedAsMissingPreparation() {
        var result = new RecipeExtraction(
                "Chocolate Mousse", null, null,
                List.of("200 g chocolate"),
                null,
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertEquals(List.of("preparation"), result.missingFields());
    }

    @Test
    void emptyStepsListIsTreatedAsMissingPreparation() {
        var result = new RecipeExtraction(
                "Chocolate Mousse", null, null,
                List.of("200 g chocolate"),
                List.of(),
                null, null, List.of(), null, null, null);

        assertFalse(result.isComplete());
        assertTrue(result.missingFields().contains("preparation"));
    }

    @Test
    void allFieldsMissingReturnsThreeMissingFields() {
        var result = new RecipeExtraction();

        assertFalse(result.isComplete());
        assertEquals(List.of("title", "ingredients", "preparation"), result.missingFields());
    }

    @Test
    void warningsArePreservedInResult() {
        var result = new RecipeExtraction(
                "Test", null, null,
                List.of("item"),
                List.of("step"),
                null, null,
                List.of("Handwriting partially illegible", "Servings unclear"),
                null, null, null);

        assertTrue(result.isComplete());
        assertEquals(2, result.warnings().size());
        assertEquals("Handwriting partially illegible", result.warnings().get(0));
    }

    @Test
    void providerAndModelMetadataArePreserved() {
        var result = new RecipeExtraction(
                "Test", null, null,
                List.of("item"),
                List.of("step"),
                null, null, List.of(), null,
                "openai", "gpt-4o");

        assertEquals("openai", result.provider());
        assertEquals("gpt-4o", result.model());
    }

    @Test
    void optionalFieldsDoNotAffectCompleteness() {
        var result = new RecipeExtraction(
                "Test", null, null,
                List.of("item"),
                List.of("step"),
                null, null, List.of(), null, null, null);

        assertTrue(result.isComplete());
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void rawModelResponseIsPreserved() {
        String rawJson = """
                {"title":"Test","ingredients":["item"],"steps":["step"]}""";
        var result = new RecipeExtraction(
                "Test", null, null,
                List.of("item"),
                List.of("step"),
                null, null, List.of(),
                rawJson, null, null);

        assertEquals(rawJson, result.rawModelResponse());
    }
}
