package be.lutske.leolegacy.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for RecipeParserService.
 * These do NOT require Quarkus — they test pure parsing logic.
 */
class RecipeParserServiceTest {

    private final RecipeParserService parser = new RecipeParserService();

    @Test
    void parseStructuredRecipeWithBothHeadings() {
        String text = """
                Chocolate Mousse
                
                Ingredients:
                200 g dark chocolate
                4 eggs
                50 g sugar
                
                Instructions:
                Melt the chocolate au bain-marie.
                Separate the eggs.
                Whip the egg whites with sugar until stiff.
                Fold into the chocolate mixture.""";

        var result = parser.parse(text);

        assertEquals("Chocolate Mousse", result.proposedRecipe().title());
        assertNotNull(result.proposedRecipe().ingredients());
        assertEquals(3, result.proposedRecipe().ingredients().size());
        assertTrue(result.proposedRecipe().ingredients().stream().anyMatch(s -> s.contains("chocolate")));
        assertNotNull(result.proposedRecipe().preparation());
        assertTrue(result.proposedRecipe().preparation().contains("Melt"));
        assertTrue(result.missingFields().isEmpty(), "Expected no missing fields but got: " + result.missingFields());
    }

    @Test
    void parseRecipeWithDutchHeadings() {
        String text = """
                Pompoensoep
                
                Benodigdheden:
                1 pompoen
                2 uien
                1 liter bouillon
                
                Bereiding:
                Snijd de pompoen in stukken.
                Bak de uien aan.
                Voeg de bouillon toe en kook 20 minuten.""";

        var result = parser.parse(text);

        assertEquals("Pompoensoep", result.proposedRecipe().title());
        assertNotNull(result.proposedRecipe().ingredients());
        assertEquals(3, result.proposedRecipe().ingredients().size());
        assertNotNull(result.proposedRecipe().preparation());
        assertTrue(result.proposedRecipe().preparation().contains("pompoen"));
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void parseRecipeWithItalianHeadings() {
        String text = """
                Tiramisù

                Ingredienti:
                3 uova
                250 g mascarpone
                200 g savoiardi

                Procedimento:
                Separa i tuorli dagli albumi.
                Monta i tuorli con lo zucchero e aggiungi il mascarpone.
                Inzuppa i savoiardi nel caffè e alternali con la crema.""";

        var result = parser.parse(text);

        assertEquals("Tiramisù", result.proposedRecipe().title());
        assertNotNull(result.proposedRecipe().ingredients());
        assertEquals(3, result.proposedRecipe().ingredients().size());
        assertNotNull(result.proposedRecipe().preparation());
        assertTrue(result.proposedRecipe().preparation().contains("savoiardi"));
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void parseRecipeWithAllCapsTitle() {
        String text = """
                CHOCOLATE MOUSSE
                A classic French dessert
                
                Ingredients:
                200 g chocolate
                4 eggs
                
                Directions:
                Melt and fold.""";

        var result = parser.parse(text);

        assertEquals("CHOCOLATE MOUSSE", result.proposedRecipe().title());
    }

    @Test
    void parseRecipeWithoutHeadingsUsesHeuristics() {
        String text = """
                Simple Cake
                
                2 cups flour
                1 cup sugar
                3 eggs
                
                Mix all ingredients together.
                Bake at 180 degrees for 30 minutes.""";

        var result = parser.parse(text);

        assertEquals("Simple Cake", result.proposedRecipe().title());
        assertTrue(result.parseWarnings().stream().anyMatch(w -> w.contains("heuristic")));
    }

    @Test
    void parseEmptyTextReturnsAllFieldsMissing() {
        var result = parser.parse("");

        assertTrue(result.missingFields().contains("title"));
        assertTrue(result.missingFields().contains("ingredients"));
        assertTrue(result.missingFields().contains("preparation"));
        assertTrue(result.parseWarnings().stream().anyMatch(w -> w.contains("no text")));
    }

    @Test
    void parseBlankTextReturnsAllFieldsMissing() {
        var result = parser.parse("   \n  \n   ");

        assertTrue(result.missingFields().contains("title"));
        assertTrue(result.missingFields().contains("ingredients"));
        assertTrue(result.missingFields().contains("preparation"));
    }

    @Test
    void parseRecipeWithOnlyIngredientsHeading() {
        String text = """
                My Recipe
                
                Ingredients:
                100 g butter
                200 g flour
                Some text that is not an ingredient""";

        var result = parser.parse(text);

        assertEquals("My Recipe", result.proposedRecipe().title());
        assertNotNull(result.proposedRecipe().ingredients());
    }

    @Test
    void parseRecipeWithOnlyInstructionsHeading() {
        String text = """
                Quick Snack
                
                Method:
                Take bread.
                Add cheese.
                Toast until golden.""";

        var result = parser.parse(text);

        assertEquals("Quick Snack", result.proposedRecipe().title());
        assertNotNull(result.proposedRecipe().preparation());
        assertTrue(result.missingFields().contains("ingredients"));
    }

    @Test
    void parseResultIncludesMissingFieldsForIncompleteRecipe() {
        String text = """
                Ingredients:
                1 cup rice
                2 cups water""";

        var result = parser.parse(text);

        assertTrue(result.missingFields().contains("preparation"));
    }

    @Test
    void parseHandlesVariousInstructionHeadingSynonyms() {
        for (String heading : new String[]{
                "Instructions:", "Directions:", "Method:", "Steps:",
                "Preparation:", "Bereiding:", "Werkwijze:"}) {
            String text = """
                    Test Recipe
                    
                    Ingredients:
                    1 item
                    
                    %s
                    Do something.""".formatted(heading);

            var result = parser.parse(text);
            assertNotNull(result.proposedRecipe().preparation(), "Failed for heading: " + heading);
        }
    }
}
