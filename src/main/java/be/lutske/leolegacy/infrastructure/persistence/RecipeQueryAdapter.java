package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.application.port.RecipeQueryPort;
import be.lutske.leolegacy.domain.Category;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeSummary;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryPanacheRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipePanacheRepository;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeQueryAdapter implements RecipeQueryPort {

    private final RecipePanacheRepository recipeRepository;
    private final CategoryPanacheRepository categoryRepository;

    public RecipeQueryAdapter(RecipePanacheRepository recipeRepository, CategoryPanacheRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> listCategories() {
        List<CategoryEntity> categories = categoryRepository.listAll(Sort.by("displayOrder").and("name"));
        return categories.stream()
                .map(category -> new Category(
                        category.id,
                        category.slug,
                        category.name,
                        recipeRepository.count("category.id", category.id)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummary> listRecipes(String categorySlug) {
        List<RecipeEntity> recipes;
        if (categorySlug == null || categorySlug.isBlank()) {
            recipes = recipeRepository.listAll(Sort.by("title"));
        } else {
            recipes = recipeRepository.list("category.slug", Sort.by("title"), categorySlug);
        }

        return recipes.stream()
                .map(recipe -> new RecipeSummary(
                        recipe.id,
                        recipe.legacyId,
                        recipe.title,
                        recipe.category.name,
                        toExcerpt(recipe.preparation)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RecipeDetail> findRecipeById(Long id) {
        return recipeRepository.findByIdOptional(id)
                .map(recipe -> new RecipeDetail(
                        recipe.id,
                        recipe.legacyId,
                        recipe.title,
                        recipe.category.name,
                        recipe.ingredients,
                        recipe.preparation,
                        recipe.pdfSlug
                ));
    }

    private String toExcerpt(String preparation) {
        if (preparation == null || preparation.isBlank()) {
            return "";
        }
        if (preparation.length() <= 160) {
            return preparation;
        }
        return preparation.substring(0, 157) + "...";
    }
}
