package be.lutske.leolegacy.domain.category;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {
    @Test
    public void testCategoryCreationWithValidName() {
        // Arrange
        String validName = "Vegetables";
        
        // Act
        Category category = new Category();
        category.setId("1");
        category.setName(validName);
        
        // Assert
        assertEquals(validName, category.getName());
    }

    @Test
    public void testCategoryCreationWithEmptyNameFails() {
        // Arrange
        String emptyName = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Category category = new Category();
            category.setId("1");
            category.setName(emptyName);
        });
    }
}