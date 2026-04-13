package be.lutske.leolegacy.port.out;

import be.lutske.leolegacy.domain.recipe.Recipe;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecipeQueryPort {

    List<Recipe> findAll();

    List<Recipe> findByCategoryId(long categoryId);

    List<Recipe> findTopByViewCount(int limit);

    List<Recipe> findBySearchTerms(Collection<String> searchTerms, int limit);

    Optional<Recipe> findById(long id);

    long countByCategoryId(long categoryId);
}
