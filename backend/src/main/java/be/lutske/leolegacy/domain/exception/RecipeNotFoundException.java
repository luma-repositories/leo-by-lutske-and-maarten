package be.lutske.leolegacy.domain.exception;

import be.lutske.leolegacy.domain.recipe.RecipeId;

public class RecipeNotFoundException extends RuntimeException {
    private final RecipeId recipeId;

    public RecipeNotFoundException(RecipeId recipeId) {
        super("Recipe not found: " + recipeId.getValue());
        this.recipeId = recipeId;
    }

    public RecipeId getRecipeId() {
        return recipeId;
    }
}