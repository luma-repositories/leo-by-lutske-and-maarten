package be.lutske.leolegacy.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeIdTest {
    @Test
    void testValidRecipeIdCreation() {
        RecipeId id = new RecipeId("123");
        assertEquals("123", id.value());
    }

    @Test
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeId(null));
    }

    @Test
    void testEmptyStringInput() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeId(""));
    }

    @Test
    void testTrimmedEmptyStringInput() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeId(" "));
    }

    @Test
    void testValueAccess() {
        RecipeId id = new RecipeId("abc123");
        assertEquals("abc123", id.value());
    }
}