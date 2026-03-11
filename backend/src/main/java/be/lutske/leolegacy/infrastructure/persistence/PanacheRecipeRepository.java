package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.mapper.RecipeMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Panache-based adapter implementing the domain {@link RecipeRepository} port.
 */
@ApplicationScoped
public class PanacheRecipeRepository implements RecipeRepository, PanacheRepository<RecipeEntity> {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Recipe> listAllRecipes() {
        return listAll().stream().map(RecipeMapper::toDomain).toList();
    }

    @Override
    public List<Recipe> findByCategoryId(long categoryId) {
        return list("category.id", categoryId).stream()
                .map(RecipeMapper::toDomain)
                .toList();
    }

    @Override
    public List<Recipe> findTopByViewCount(int limit) {
        return list("ORDER BY viewCount DESC").stream()
                .limit(limit)
                .map(RecipeMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Recipe> findById(long id) {
        RecipeEntity entity = find("id", id).firstResult();
        return Optional.ofNullable(entity).map(RecipeMapper::toDomain);
    }

    @Override
    public Recipe save(Recipe recipe) {
        var entity = new RecipeEntity();
        CategoryEntity categoryEntity = null;
        if (recipe.getCategory() != null && recipe.getCategory().getId() != null) {
            categoryEntity = entityManager.find(CategoryEntity.class, recipe.getCategory().getId());
        }
        RecipeMapper.toEntity(recipe, entity, categoryEntity);
        persist(entity);
        return RecipeMapper.toDomain(entity);
    }

    /**
     * Provides access to count by category for the category listing use case.
     */
    public long countByCategoryId(long categoryId) {
        return count("category.id", categoryId);
    }
}
