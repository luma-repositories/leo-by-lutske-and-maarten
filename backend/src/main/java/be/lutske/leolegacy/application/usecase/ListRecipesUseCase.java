package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.repository.RecipeRepository;
import java.util.List;

public class ListRecipesUseCase {
    private final RecipeRepository recipeRepository;

    public ListRecipesUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> execute() {
        return recipeRepository.findAll();
    }
}