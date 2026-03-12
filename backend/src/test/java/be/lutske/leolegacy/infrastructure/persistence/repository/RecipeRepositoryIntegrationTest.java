package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

@SpringJUnitConfig
@SpringBootTest
public class RecipeRepositoryIntegrationTest {
    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    public void testSaveAndFindRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setIngredients("Flour, Eggs");
        recipe.setInstructions("Mix and bake");
        
        Recipe saved = recipeRepository.save(recipe);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        
        Optional<Recipe> found = recipeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Recipe", found.get().getTitle());
    }
}