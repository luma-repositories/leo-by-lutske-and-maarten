package be.lutske.leolegacy.test.usecase;

import be.lutske.leolegacy.application.service.RecipeExtractionService;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.test.doubles.RecipeRepositoryTestDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecipeUseCaseTest {
    private RecipeExtractionService service;
    private RecipeRepositoryTestDouble repository;

    @BeforeEach
    public void setUp() {
        repository = new RecipeRepositoryTestDouble();
        service = new RecipeExtractionService(repository);
    }

    @Test
    public void testExtractRecipe() {
        // Arrange
        String markdown = "# Test Recipe\n\n## Ingredients\n- Flour\n- Eggs\n\n## Preparation\nMix and bake";
        
        // Act
        Recipe recipe = service.extractRecipe(markdown);
        
        // Assert
        assertNotNull(recipe);
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("Flour, Eggs", recipe.getIngredients());
        assertEquals("Mix and bake", recipe.getInstructions());
    }
}