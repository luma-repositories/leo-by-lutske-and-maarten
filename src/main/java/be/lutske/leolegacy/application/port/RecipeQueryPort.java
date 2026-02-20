package be.lutske.leolegacy.application.port;

import be.lutske.leolegacy.domain.Category;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeSummary;

import java.util.List;
import java.util.Optional;

public interface RecipeQueryPort {
    List<Category> listCategories();

    List<RecipeSummary> listRecipes(String categorySlug);

    Optional<RecipeDetail> findRecipeById(Long id);
}
