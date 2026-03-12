package be.lutske.leolegacy.test.usecase;

import be.lutske.leolegacy.application.service.CategoryService;
import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.test.doubles.CategoryRepositoryTestDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryUseCaseTest {
    private CategoryService service;
    private CategoryRepositoryTestDouble repository;

    @BeforeEach
    public void setUp() {
        repository = new CategoryRepositoryTestDouble();
        service = new CategoryService(repository);
    }

    @Test
    public void testCreateCategory() {
        // Arrange
        String name = "Vegetables";
        
        // Act
        Category category = service.createCategory(name);
        
        // Assert
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals("1", category.getId()); // Assuming ID is generated as "1" for test
    }
}