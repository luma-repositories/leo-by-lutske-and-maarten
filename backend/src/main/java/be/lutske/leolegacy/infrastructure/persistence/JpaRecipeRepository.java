package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeTitle;
import be.lutske.leolegacy.domain.recipe.Ingredients;
import be.lutske.leolegacy.domain.recipe.Preparation;
import be.lutske.leolegacy.domain.viewcount.ViewCount;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeTitle;
import be.lutske.leolegacy.domain.recipe.Ingredients;
import be.lutske.leolegacy.domain.recipe.Preparation;
import be.lutske.leolegacy.domain.viewcount.ViewCount;
import be.lutske.leolegacy.domain.entity.RecipeEntity;
import be.lutske.leolegacy.domain.valueobject.RecipeId;
import be.lutske.leolegacy.domain.valueobject.CategoryId;
import be.lutske.leolegacy.domain.repository.RecipeRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.List;

@ApplicationScoped
public class JpaRecipeRepository implements RecipeRepository {

    @Inject
    be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository panacheRepository;

    @Override
    public Optional<Recipe> findById(RecipeId id) {
        return panacheRepository.findByIdOptional(Long.parseLong(id.value()))
                .map(this::mapToDomain);
    }

    @Override
    public List<Recipe> findAll() {
        return panacheRepository.listAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Recipe> findByCategory(CategoryId categoryId) {
        return panacheRepository.findByCategoryId(Long.parseLong(categoryId.value())).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void save(Recipe recipe) {
        RecipeEntity entity = mapToEntity(recipe);
        panacheRepository.persist(entity);
    }

    @Override
    public void delete(RecipeId id) {
        panacheRepository.deleteById(Long.parseLong(id.value()));
    }

    private Recipe mapToDomain(RecipeEntity entity) {
        return new Recipe(
            new RecipeTitle(entity.getTitle()),
            new Ingredients(entity.getIngredients()),
            new Preparation(entity.getPreparation()),
            new ViewCount(entity.getViewCount())
        );
    }

    private RecipeEntity mapToEntity(Recipe domain) {
        return new RecipeEntity(
            domain.getTitle().getValue(),
            domain.getIngredients().getValue(),
            domain.getPreparation().getValue(),
            domain.getViewCount().getValue()
        );
    }

}