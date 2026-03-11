package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.Category;
import be.lutske.leolegacy.domain.port.CategoryRepository;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Use case: list all categories with their recipe counts.
 */
@ApplicationScoped
public class ListCategories {

    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    public ListCategories(CategoryRepository categoryRepository, RecipeRepository recipeRepository) {
        this.categoryRepository = categoryRepository;
        this.recipeRepository = recipeRepository;
    }

    public record CategoryWithCount(Category category, long recipeCount) {}

    public List<CategoryWithCount> execute() {
        return categoryRepository.findAllOrderedByName().stream()
                .map(cat -> new CategoryWithCount(
                        cat,
                        recipeRepository.findByCategoryId(cat.getId()).size()
                ))
                .toList();
    }
}
