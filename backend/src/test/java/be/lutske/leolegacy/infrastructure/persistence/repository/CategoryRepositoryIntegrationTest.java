package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

@SpringJUnitConfig
@SpringBootTest
public class CategoryRepositoryIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void testSaveAndFindCategory() {
        Category category = new Category();
        category.setName("Dessert");
        
        Category saved = categoryRepository.save(category);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        
        Optional<Category> found = categoryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Dessert", found.get().getName());
    }
}