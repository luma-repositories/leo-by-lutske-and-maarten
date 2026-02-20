package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.RecipeQueryPort;
import be.lutske.leolegacy.domain.Category;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ListCategoriesUseCase {

    private final RecipeQueryPort recipeQueryPort;

    public ListCategoriesUseCase(RecipeQueryPort recipeQueryPort) {
        this.recipeQueryPort = recipeQueryPort;
    }

    public List<Category> execute() {
        return recipeQueryPort.listCategories();
    }
}
