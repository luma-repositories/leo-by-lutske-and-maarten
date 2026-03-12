package be.lutske.leolegacy.domain.recipe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecipeTest {
    @Test
    public void testRecipeCreation() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Test Recipe");
        recipe.setIngredients("Flour, Eggs");
        recipe.setInstructions("Mix and bake");
        
        assertNotNull(recipe);
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("Flour, Eggs", recipe.getIngredients());
        assertEquals("Mix and bake", recipe.getInstructions());
    }
}