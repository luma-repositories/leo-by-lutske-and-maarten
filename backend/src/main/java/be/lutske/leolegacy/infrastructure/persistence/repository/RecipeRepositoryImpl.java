package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class RecipeRepositoryImpl implements RecipeRepository {
    private final EntityManager entityManager;

    public RecipeRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        RecipeEntity entity = entityManager.find(RecipeEntity.class, id);
        return entity != null ? Optional.of(toDomain(entity)) : Optional.empty();
    }

    @Override
    public List<Recipe> findAll() {
        TypedQuery<RecipeEntity> query = entityManager.createQuery("SELECT e FROM RecipeEntity e", RecipeEntity.class);
        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Recipe save(Recipe recipe) {
        RecipeEntity entity = toEntity(recipe);
        entity = entityManager.merge(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        RecipeEntity entity = entityManager.find(RecipeEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<Recipe> findByCategoryId(Long categoryId) {
        TypedQuery<RecipeEntity> query = entityManager.createQuery(
                "SELECT e FROM RecipeEntity e WHERE e.category.id = :categoryId",
                RecipeEntity.class
        );
        query.setParameter("categoryId", categoryId);
        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Recipe> findTopByViewCount(int count) {
        TypedQuery<RecipeEntity> query = entityManager.createQuery(
                "SELECT e FROM RecipeEntity e ORDER BY e.viewCount DESC",
                RecipeEntity.class
        );
        query.setMaxResults(count);
        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    private RecipeEntity toEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(recipe.getId());
        entity.setTitle(recipe.getTitle());
        entity.setDescription(recipe.getDescription());
        entity.setImageUrl(recipe.getImageUrl());
        entity.setIngredients(recipe.getIngredients());
        entity.setInstructions(recipe.getInstructions());
        entity.setViewCount(recipe.getViewCount());
        entity.setCategory(recipe.getCategory());
        return entity;
    }

    private Recipe toDomain(RecipeEntity entity) {
        return new Recipe()
                .setId(entity.getId())
                .setTitle(entity.getTitle())
                .setDescription(entity.getDescription())
                .setImageUrl(entity.getImageUrl())
                .setIngredients(entity.getIngredients())
                .setInstructions(entity.getInstructions())
                .setViewCount(entity.getViewCount())
                .setCategory(entity.getCategory());
    }
}