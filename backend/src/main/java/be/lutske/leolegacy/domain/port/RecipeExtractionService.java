package be.lutske.leolegacy.domain.port;

import be.lutske.leolegacy.domain.model.RecipeExtraction;

/**
 * Port for extracting structured recipe data from an image.
 * Infrastructure provides the LLM-based implementation.
 */
public interface RecipeExtractionService {

    RecipeExtraction extractRecipeFromImage(byte[] imageBytes, String mimeType);
}
