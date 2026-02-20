package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.RecipeQueryPort;
import be.lutske.leolegacy.domain.RecipeDetail;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class GetRecipeDetailUseCase {

    private final RecipeQueryPort recipeQueryPort;

    public GetRecipeDetailUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public Optional<RecipeDetail> execute(Long id) {
        return recipeQueryPort.findRecipeById(id);
    }
}
