package be.lutske.leolegacy.usecase.recipe;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.port.out.RecipeQueryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class GetRecipeUseCase {

    private final RecipeQueryPort recipeQueryPort;

    public GetRecipeUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public Optional<Recipe> execute(long recipeId) {
        return recipeQueryPort.findById(recipeId);
    }
}
