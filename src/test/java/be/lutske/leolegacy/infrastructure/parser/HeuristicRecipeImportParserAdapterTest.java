package be.lutske.leolegacy.infrastructure.parser;

import be.lutske.leolegacy.domain.RecipeImportAnalysis;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeuristicRecipeImportParserAdapterTest {

    private final HeuristicRecipeImportParserAdapter parser = new HeuristicRecipeImportParserAdapter("Imported from image");

    @Test
    void shouldExtractStructuredSectionsWithHeadings() {
        String rawText = "Tomato Soup\nIngredients\n2 cups tomatoes\n1 tsp salt\nInstructions\nMix and cook";

        RecipeImportAnalysis result = parser.parse(rawText);

        assertEquals("Tomato Soup", result.proposedRecipe().title());
        assertTrue(result.proposedRecipe().ingredients().contains("2 cups tomatoes"));
        assertTrue(result.proposedRecipe().instructions().contains("Mix and cook"));
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void shouldAskForMoreInfoWhenTextIsAmbiguous() {
        RecipeImportAnalysis result = parser.parse("Random shopping note");

        assertFalse(result.parseWarnings().isEmpty());
        assertTrue(result.needsMoreInfo());
        assertTrue(result.missingFields().contains("ingredients"));
        assertTrue(result.missingFields().contains("instructions"));
    }
}
