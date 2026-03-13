package be.lutske.leolegacy.domain.exception;

import be.lutske.leolegacy.domain.recipe.RecipeId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecipeNotFoundExceptionTest {
    @Test
    public void testConstructorSetsMessageAndRecipeId() {
        RecipeId id = new RecipeId("123");
        RecipeNotFoundException exception = new RecipeNotFoundException(id);
        assertEquals("Recipe not found: 123", exception.getMessage());
        assertEquals(id, exception.getRecipeId());
    }

    @Test
    public void testMessageContainsRecipeId() {
        RecipeId id = new RecipeId("456");
        RecipeNotFoundException exception = new RecipeNotFoundException(id);
        assertTrue(exception.getMessage().contains(id.getValue()));
    }

    @Test
    public void testGetRecipeIdReturnsOriginal() {
        RecipeId id = new RecipeId("789");
        RecipeNotFoundException exception = new RecipeNotFoundException(id);
        assertEquals(id, exception.getRecipeId());
    }
}