package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.Category;
import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.domain.port.CategoryRepository;
import be.lutske.leolegacy.domain.port.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Use case: confirm and save a recipe that was extracted from an image.
 */
@ApplicationScoped
public class ConfirmRecipeImport {

    private static final long DEFAULT_IMPORT_CATEGORY_ID = 15L;

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public ConfirmRecipeImport(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    public record ConfirmRequest(
            String title,
            List<String> ingredients,
            String preparation,
            String source,
            Long categoryId,
            String rawModelResponse
    ) {}

    @Transactional
    public Recipe execute(ConfirmRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Ingredients are required");
        }
        if (request.preparation() == null || request.preparation().isBlank()) {
            throw new IllegalArgumentException("Preparation is required");
        }

        long categoryId = request.categoryId() != null ? request.categoryId() : DEFAULT_IMPORT_CATEGORY_ID;
        Category category = categoryRepository.findById(categoryId)
                .or(() -> categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID))
                .orElseThrow(() -> new IllegalStateException("Default import category not found"));

        var recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setIngredients(request.ingredients());
        recipe.setPreparation(request.preparation());
        recipe.setCategory(category);
        recipe.setViewCount(0);
        recipe.setSource(request.source() != null ? request.source() : "Imported from image");
        recipe.setCreatedAt(Instant.now());
        recipe.setImportMetadata(request.rawModelResponse());

        return recipeRepository.save(recipe);
    }
}
