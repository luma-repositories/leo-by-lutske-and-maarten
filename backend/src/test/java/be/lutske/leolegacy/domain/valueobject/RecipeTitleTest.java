package be.lutske.leolegacy.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecipeTitleTest {
    
    @Test
    public void validRecipeTitleIsAccepted() {
        RecipeTitle title = new RecipeTitle("Pancake Recipe");
        assertEquals("Pancake Recipe", title.value());
    }

    @Test
    public void nullInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeTitle(null));
    }

    @Test
    public void emptyInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeTitle(""));
    }

    @Test
    public void longInputThrowsException() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            sb.append("a");
        }
        assertThrows(IllegalArgumentException.class, () -> new RecipeTitle(sb.toString()));
    }

    @Test
    public void valueMethodReturnsOriginalString() {
        RecipeTitle title = new RecipeTitle("Biscuit Recipe");
        assertEquals("Biscuit Recipe", title.value());
    }
}