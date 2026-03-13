package be.lutske.leolegacy.domain.recipe.repository;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeId;
import be.lutske.leolegacy.domain.category.CategoryId;
import java.util.List;

public interface RecipeRepository {
    Recipe findById(RecipeId id);
    List<Recipe> findAll();
    List<Recipe> findByCategory(CategoryId categoryId);
    void save(Recipe recipe);
}