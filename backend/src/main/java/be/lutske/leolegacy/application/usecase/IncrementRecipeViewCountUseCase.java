package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeId;
import be.lutske.leolegacy.domain.recipe.repository.RecipeRepository;
import be.lutske.leolegacy.domain.exception.RecipeNotFoundException;

public class IncrementRecipeViewCountUseCase {
    private final RecipeRepository recipeRepository;

    public IncrementRecipeViewCountUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe execute(RecipeId id) {
        Recipe recipe = recipeRepository.findById(id);
        if (recipe == null) {
            throw new RecipeNotFoundException(id);
        }
        Recipe updatedRecipe = recipe.incrementViewCount();
        recipeRepository.save(updatedRecipe);
        return updatedRecipe;
    }
}