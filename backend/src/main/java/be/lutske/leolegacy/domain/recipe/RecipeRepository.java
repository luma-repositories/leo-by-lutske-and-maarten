package be.lutske.leolegacy.domain.recipe;

import be.lutske.leolegacy.domain.category.CategoryId;
import be.lutske.leolegacy.domain.recipe.Recipe;

import java.util.Optional;

public interface RecipeRepository {
    Optional<Recipe> findById(RecipeId id);
    java.util.stream.Stream<Recipe> findByCategory(CategoryId categoryId);
    Recipe save(Recipe recipe);
    void delete(Recipe recipe);
}