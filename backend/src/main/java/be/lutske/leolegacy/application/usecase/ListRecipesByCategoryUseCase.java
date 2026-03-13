package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.category.CategoryId;
import be.lutske.leolegacy.domain.recipe.repository.RecipeRepository;
import java.util.List;

public class ListRecipesByCategoryUseCase {
    private final RecipeRepository recipeRepository;

    public ListRecipesByCategoryUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> execute(CategoryId categoryId) {
        return recipeRepository.findByCategory(categoryId);
    }
}