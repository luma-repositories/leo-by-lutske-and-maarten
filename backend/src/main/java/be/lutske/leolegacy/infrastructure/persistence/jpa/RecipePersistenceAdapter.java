package be.lutske.leolegacy.infrastructure.persistence.jpa;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import be.lutske.leolegacy.port.out.RecipeCommandPort;
import be.lutske.leolegacy.port.out.RecipeQueryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RecipePersistenceAdapter implements RecipeQueryPort, RecipeCommandPort {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public RecipePersistenceAdapter(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Recipe> findAll() {
        return recipeRepository.listAll().stream()
                .map(RecipeEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Recipe> findByCategoryId(long categoryId) {
        return recipeRepository.findByCategoryId(categoryId).stream()
                .map(RecipeEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Recipe> findTopByViewCount(int limit) {
        return recipeRepository.findTopByViewCount(limit).stream()
                .map(RecipeEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Recipe> findById(long id) {
        return Optional.ofNullable(recipeRepository.findById(id))
                .map(RecipeEntityMapper::toDomain);
    }

    @Override
    public long countByCategoryId(long categoryId) {
        return recipeRepository.countByCategoryId(categoryId);
    }

    @Override
    public Recipe saveImportedRecipe(RecipeImportDraft draft) {
        var category = categoryRepository.findById(draft.categoryId());
        if (category == null) {
            throw new IllegalStateException("Category with id " + draft.categoryId() + " not found");
        }
        var entity = RecipeEntityMapper.fromImportDraft(draft, category);
        recipeRepository.persist(entity);
        return RecipeEntityMapper.toDomain(entity);
    }
}
