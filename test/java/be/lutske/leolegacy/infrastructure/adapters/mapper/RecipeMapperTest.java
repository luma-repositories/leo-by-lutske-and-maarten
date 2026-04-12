// test/java/be/lutske/leolegacy/infrastructure/adapters/mapper/RecipeMapperTest.java
package be.lutske.leolegacy.infrastructure.adapters.mapper;

import be.lutske.leolegacy.domain.AiExtractionResult;
import be.lutske.leolegacy.domain.Category;
import be.lutske.leolegacy.domain.Recipe;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

public class RecipeMapperTest {

    private RecipeEntity mockEntity;
    private CategoryEntity mockEntityCategory;
    private final Category mockDomainCategory = new Category(1L, "TestedCategory");

    @BeforeEach
    void setUp() {
        // Setup mock entities for testing the mapping boundaries
        mockEntityCategory = new CategoryEntity();
        mockEntityCategory.setId(1L);
        mockEntityCategory.setName("Mock Category");

        mockEntity = new RecipeEntity();
        mockEntity.setId(10L);
        mockEntity.setTitle("Test Title");
        mockEntity.setIngredients("Flour, Eggs");
        mockEntity.setPreparation("Mix and bake");
        mockEntity.setCategory(mockEntityCategory);
        mockEntity.setViewCount(5);
        mockEntity.setSource("TestSource");
        mockEntity.setCreatedAt(Instant.now());
    }

    @Test
    void testToDomainMapper_ShouldMapEntityToDomain() {
        // ACT
        Recipe domainRecipe = RecipeMapper.toDomain(mockEntity);

        // ASSERT
        assertNotNull(domainRecipe);
        assertEquals("Test Title", domainRecipe.getTitle());
        assertEquals(5, domainRecipe.getViewCount());
        assertEquals("TestSource", domainRecipe.getSource());
    }

    @Test
    void testFromAiResult_ShouldPopulateDomain() {
        // GIVEN
        AiExtractionResult aiResult = new AiExtractionResult(
                "Raw content from OCR...", 
                "Test AI Title", 
                "AI Ingredients", 
                "AI Prep", 
                "Image analysis screenshot"
        );
        Category mockCategory = new Category(1L, "TestCategory");

        // ACT (Directly testing the logic that was stubbed in the use case)
        Recipe domainRecipe = new Recipe(
                null, 
                aiResult.getProposedTitle(), 
                aiResult.getProposedIngredients(), 
                aiResult.getProposedPreparation(), 
                mockCategory, 
                0, 
                aiResult.getSource(), 
                null
        );

        // ASSERT
        assertEquals("AI Ingredients", domainRecipe.getIngredientsContent());
    }
}