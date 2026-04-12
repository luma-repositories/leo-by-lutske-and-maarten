package be.lutske.leolegacy.ports.repository;

import be.lutske.leolegacy.domain.Recipe;
import be.lutske.leolegacy.domain.Category;
import java.util.List;
import java.util.Optional;

/**
 * Port Interface for Recipe data access. Defines domain interaction contracts.
 */
public interface RecipeRepositoryPort {
    
    Optional<Recipe> findById(Long id);

    List<Recipe> findAll(String categoryName, int limit, int offset);
    
    List<Category> findAllCategories();

    /** Saves a new recipe or updates an existing one based on its ID. */
    Recipe save(Recipe recipe);
}