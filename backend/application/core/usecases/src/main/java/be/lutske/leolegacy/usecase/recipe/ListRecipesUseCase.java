package be.lutske.leolegacy.usecase.recipe;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.port.out.RecipeQueryPort;

import java.util.List;

public class ListRecipesUseCase {

    private final RecipeQueryPort recipeQueryPort;

    public ListRecipesUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public List<Recipe> listAllOrByCategory(Long categoryId) {
        return categoryId == null ? recipeQueryPort.findAll() : recipeQueryPort.findByCategoryId(categoryId);
    }

    public List<Recipe> listTopRecipes(int limit) {
        return recipeQueryPort.findTopByViewCount(limit);
    }
}
