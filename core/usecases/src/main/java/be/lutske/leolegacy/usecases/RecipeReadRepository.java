package be.lutske.leolegacy.usecases;

import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeSummary;

import java.util.List;
import java.util.Optional;

public interface RecipeReadRepository {
    List<RecipeSummary> listAll();
    List<RecipeSummary> findByCategoryId(long categoryId);
    List<RecipeSummary> findTopByViewCount(int limit);
    Optional<RecipeDetail> findById(long recipeId);
}