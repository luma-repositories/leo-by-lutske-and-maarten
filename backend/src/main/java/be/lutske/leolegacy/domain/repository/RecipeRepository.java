package be.lutske.leolegacy.domain.repository;

import be.lutske.leolegacy.domain.entity.Recipe;
import be.lutske.leolegacy.domain.valueobject.RecipeId;
import be.lutske.leolegacy.domain.valueobject.CategoryId;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    Optional<Recipe> findById(RecipeId id);
    List<Recipe> findAll();
    List<Recipe> findByCategory(CategoryId categoryId);
    void save(Recipe recipe);
    void delete(RecipeId id);
}