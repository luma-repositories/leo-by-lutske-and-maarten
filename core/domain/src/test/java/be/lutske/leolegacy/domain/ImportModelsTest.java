package be.lutske.leolegacy.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportModelsTest {

    @Test
    void testEmptyExtraction() {
        ExtractionResult result = new ExtractionResult();
        List<String> missingFields = result.missingFields();
        assertEquals(3, missingFields.size());
        assertEquals("title", missingFields.get(0));
        assertEquals("ingredients", missingFields.get(1));
        assertEquals("preparation", missingFields.get(2));
        assertNotNull(result.warnings());
        assertEquals(List.of(), result.warnings());
        assertFalse(result.isComplete());
    }

    @Test
    void testCompleteExtraction() {
        List<String> ingredients = List.of("flour");
        List<String> steps = List.of("Mix", "Bake");
        ExtractionResult result = new ExtractionResult("Test Recipe", "A test recipe",
                                                      "4–6 servings", ingredients, steps,
                                                      "Imported", List.of("imported"),
                                                      List.of("info"),
                                                      "Model response", "OpenAI", "gpt-4");
        assertEquals(0, result.missingFields().size());
        assertTrue(result.isComplete());
    }

    @Test
    void testPartialExtraction() {
        List<String> ingredients = List.of("flour");
        ExtractionResult result = new ExtractionResult("Test Recipe", "A test recipe",
                                                      null, ingredients, null,
                                                      "Imported", List.of("imported"),
                                                      null,
                                                      "Model response", "OpenAI", "gpt-4");
        // Has title and ingredients, no steps -> missing steps
        assertEquals(1, result.missingFields().size());
        assertEquals("preparation", result.missingFields().get(0));
        assertFalse(result.isComplete());
    }

    @Test
    void testListsWithBlankStrings() {
        List<String> ingredients = List.of("flour", "", "eggs");
        List<String> tags = List.of("imported", "");
        ExtractionResult result = new ExtractionResult("Test Recipe", null, null,
                                                      ingredients, null,
                                                      "Imported", tags,
                                                      null,
                                                      "Model response", "OpenAI", "gpt-4");
        // Should still be incomplete if ingredients is not empty (non-empty but contains blank)
        assertEquals(1, result.missingFields().size());
        assertEquals("preparation", result.missingFields().get(0));
    }

    @Test
    void testExtractionWithWhitespaces() {
        ExtractionResult result = new ExtractionResult("  Test Recipe  ", null, null,
                                                      List.of("  flour  "), null,
                                                      "Imported", List.of("imported"),
                                                      List.of("info"),
                                                      "Model response", "OpenAI", "gpt-4");
        // title is present (non-blank), ingredients is present (non-empty), steps is missing
        assertEquals(1, result.missingFields().size());
        assertEquals("preparation", result.missingFields().get(0));
        assertNotNull(result.title());
        // Ingredients should be present with trimmed values
        assertEquals("  flour  ", result.ingredients().get(0));
    }

    @Test
    void testExtractionExceptionWithMessage() {
        RecipeExtractionException exception = new RecipeExtractionException("Test error");
        assertEquals("Test error", exception.getMessage());
    }

    @Test
    void testExtractionExceptionWithCause() {
        RecipeExtractionException exception = new RecipeExtractionException("Test error", new RuntimeException("Cause"));
        assertEquals("Test error", exception.getMessage());
        assertEquals("Cause", exception.getCause().getMessage());
    }

    @Test
    void testRecipeReviewResult() {
        ProposedRecipe proposal = new ProposedRecipe("Test", "Description", "4 servings",
                                                    List.of("Ingredient"), "Steps",
                                                    "Source", List.of("tag1"), 15L,
                                                    "Notes", List.of(new IngredientConversion("1g", "1g", "g", "Ingredient1", "1g (1g) Ingredient1", true)));
        RecipeReviewResult result = new RecipeReviewResult("COMPLETE", "Model response", proposal, List.of(), List.of());
        assertEquals("COMPLETE", result.status());
        assertEquals("Model response", result.rawModelResponse());
        assertNotNull(result.proposedRecipe());
        assertEquals(List.of(), result.missingFields());
        assertEquals(List.of(), result.warnings());
    }
}