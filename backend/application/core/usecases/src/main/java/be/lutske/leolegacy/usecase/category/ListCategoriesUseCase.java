package be.lutske.leolegacy.usecase.category;

import be.lutske.leolegacy.port.out.CategoryQueryPort;
import be.lutske.leolegacy.port.out.RecipeQueryPort;

import java.util.List;

public class ListCategoriesUseCase {

    private final CategoryQueryPort categoryQueryPort;
    private final RecipeQueryPort recipeQueryPort;

    public ListCategoriesUseCase(CategoryQueryPort categoryQueryPort, RecipeQueryPort recipeQueryPort) {
        this.categoryQueryPort = categoryQueryPort;
        this.recipeQueryPort = recipeQueryPort;
    }

    public List<CategoryOverview> execute() {
        return categoryQueryPort.findAllOrderedByName().stream()
                .map(category -> new CategoryOverview(
                        category.id(),
                        category.name(),
                        recipeQueryPort.countByCategoryId(category.id())
                ))
                .toList();
    }
}
