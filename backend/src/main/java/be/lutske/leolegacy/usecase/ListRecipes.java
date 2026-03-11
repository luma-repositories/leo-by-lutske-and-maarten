package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Use case: list recipes, optionally filtered by category.
 */
@ApplicationScoped
public class ListRecipes {

    private final RecipeRepository recipeRepository;

    public ListRecipes(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> execute(Long categoryId) {
        if (categoryId != null) {
            return recipeRepository.findByCategoryId(categoryId);
        }
        return recipeRepository.listAllRecipes();
    }

    public List<Recipe> topByViewCount(int limit) {
        return recipeRepository.findTopByViewCount(limit);
    }
}
