package be.lutske.leolegacy.domain.port;

import be.lutske.leolegacy.domain.model.Recipe;

import java.util.List;
import java.util.Optional;

/**
 * Port for recipe persistence. Infrastructure provides the implementation.
 */
public interface RecipeRepository {

    List<Recipe> listAllRecipes();

    List<Recipe> findByCategoryId(long categoryId);

    List<Recipe> findTopByViewCount(int limit);

    Optional<Recipe> findById(long id);

    Recipe save(Recipe recipe);
}
