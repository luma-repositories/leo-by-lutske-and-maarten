package be.lutske.leolegacy.infrastructure.persistence;

import be.lutske.leolegacy.application.port.RecipeQueryPort;
import be.lutske.leolegacy.application.port.RecipeCommandPort;
import be.lutske.leolegacy.domain.Category;
import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeSummary;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryPanacheRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipePanacheRepository;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeQueryAdapter implements RecipeQueryPort, RecipeCommandPort {

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
                        recipe.description,
                        recipe.servings,
                        recipe.ingredients,
                        recipe.preparation,
                        recipe.pdfSlug,
                        recipe.tags,
                        recipe.source
                ));
    }

    @Override
    @Transactional
    public RecipeDetail createImportedRecipe(ProposedRecipe proposedRecipe, String rawText, String notes) {
        CategoryEntity importedCategory = categoryRepository.find("slug", "imported")
                .firstResultOptional()
                .orElseGet(this::createImportedCategory);

        RecipeEntity entity = new RecipeEntity();
        entity.legacyId = null;
        entity.title = proposedRecipe.title();
        entity.description = proposedRecipe.description();
        entity.servings = proposedRecipe.servings();
        entity.ingredients = proposedRecipe.ingredients();
        entity.preparation = proposedRecipe.instructions();
        entity.tags = proposedRecipe.tags();
        entity.source = proposedRecipe.source();
        entity.ocrRawText = rawText;
        entity.importNotes = notes;
        entity.category = importedCategory;
        entity.createdAt = LocalDateTime.now();

        recipeRepository.persist(entity);

        return new RecipeDetail(
                entity.id,
                entity.legacyId,
                entity.title,
                entity.category.name,
                entity.description,
                entity.servings,
                entity.ingredients,
                entity.preparation,
                entity.pdfSlug,
                entity.tags,
                entity.source
        );
    }

    private CategoryEntity createImportedCategory() {
        CategoryEntity category = new CategoryEntity();
        category.slug = "imported";
        category.name = "Imported";
        category.displayOrder = 1000;
        categoryRepository.persist(category);
        return category;
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
