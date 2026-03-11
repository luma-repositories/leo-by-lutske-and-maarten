package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Use case: get a single recipe by ID.
 */
@ApplicationScoped
public class GetRecipeDetail {

    private final RecipeRepository recipeRepository;

    public GetRecipeDetail(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Optional<Recipe> execute(long id) {
        return recipeRepository.findById(id);
    }
}
