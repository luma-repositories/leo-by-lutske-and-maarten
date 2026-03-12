package be.lutske.leolegacy.domain.recipe;

import be.lutske.leolegacy.domain.recipe.Recipe;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    Optional<Recipe> findById(Long id);
    List<Recipe> findAll();
    Recipe save(Recipe recipe);
    void deleteById(Long id);
    List<Recipe> findByCategoryId(Long categoryId);
    List<Recipe> findTopByViewCount(int count);
}