package be.lutske.leolegacy.usecase.recipe;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeImportDraft;
import be.lutske.leolegacy.port.out.CategoryQueryPort;
import be.lutske.leolegacy.port.out.RecipeCommandPort;

import java.time.Instant;
import java.util.List;

public class ConfirmRecipeImportUseCase {

    static final long DEFAULT_IMPORT_CATEGORY_ID = 15L;
    private static final String DEFAULT_IMPORT_SOURCE = "Imported from image";

    private final RecipeCommandPort recipeCommandPort;
    private final CategoryQueryPort categoryQueryPort;

    public ConfirmRecipeImportUseCase(RecipeCommandPort recipeCommandPort, CategoryQueryPort categoryQueryPort) {
        this.recipeCommandPort = recipeCommandPort;
        this.categoryQueryPort = categoryQueryPort;
    }

    public Recipe execute(ConfirmRecipeImportCommand command) {
        var proposed = command.proposedRecipe();
        var overrides = command.userOverrides();

        String title = override(overrides != null ? overrides.title() : null, proposed.title());
        List<String> ingredients = override(overrides != null ? overrides.ingredients() : null, proposed.ingredients());
        String preparation = override(overrides != null ? overrides.preparation() : null, proposed.preparation());
        Long requestedCategoryId = override(overrides != null ? overrides.categoryId() : null, proposed.categoryId());
        String notes = overrides != null ? overrides.notes() : proposed.notes();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients are required");
        }
        if (preparation == null || preparation.isBlank()) {
            throw new IllegalArgumentException("Preparation is required");
        }

        long categoryId = resolveCategoryId(requestedCategoryId);
        String finalPreparation = appendNotes(preparation, notes);

        return recipeCommandPort.saveImportedRecipe(new RecipeImportDraft(
                title,
                ingredients,
                finalPreparation,
                categoryId,
                proposed.source() != null ? proposed.source() : DEFAULT_IMPORT_SOURCE,
                Instant.now(),
                command.rawModelResponse()
        ));
    }

    private long resolveCategoryId(Long requestedCategoryId) {
        if (requestedCategoryId != null && categoryQueryPort.findById(requestedCategoryId).isPresent()) {
            return requestedCategoryId;
        }
        return categoryQueryPort.findById(DEFAULT_IMPORT_CATEGORY_ID)
                .map(category -> category.id())
                .orElseThrow(() -> new IllegalStateException("Default import category not found"));
    }

    private String appendNotes(String preparation, String notes) {
        if (notes == null || notes.isBlank()) {
            return preparation;
        }
        return preparation + "\n\nNotes: " + notes;
    }

    private <T> T override(T overrideValue, T originalValue) {
        return overrideValue != null ? overrideValue : originalValue;
    }
}
