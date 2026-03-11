package be.lutske.leolegacy.usecase;

import be.lutske.leolegacy.domain.model.RecipeExtraction;
import be.lutske.leolegacy.domain.port.RecipeExtractionService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Use case: extract recipe data from an uploaded image via AI.
 */
@ApplicationScoped
public class ExtractRecipeFromImage {

    private final RecipeExtractionService extractionService;

    public ExtractRecipeFromImage(RecipeExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    public RecipeExtraction execute(byte[] imageBytes, String mimeType) {
        return extractionService.extractRecipeFromImage(imageBytes, mimeType);
    }
}
