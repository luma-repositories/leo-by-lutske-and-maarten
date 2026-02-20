package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.RecipeQueryPort;
import be.lutske.leolegacy.domain.RecipeSummary;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ListRecipesUseCase {

    private final RecipeQueryPort recipeQueryPort;

    public ListRecipesUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public List<RecipeSummary> execute(String categorySlug) {
        return recipeQueryPort.listRecipes(categorySlug);
    }
}
