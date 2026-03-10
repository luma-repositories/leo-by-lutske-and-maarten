package be.lutske.leolegacy.application.service;

/**
 * Provider-agnostic interface for extracting structured recipe data from an image.
 *
 * Implementations use an LLM (via LangChain4j) to analyze the image and return
 * a structured extraction result. The provider (OpenAI, Claude, vLLM) is selected
 * via configuration — business logic never depends on a specific provider.
 */
public interface RecipeExtractionService {

    /**
     * Extract recipe data from an image.
     *
     * @param imageBytes the raw image file bytes
     * @param mimeType   the MIME type of the image (e.g. "image/png", "image/jpeg")
     * @return an {@link ExtractionResult} containing the proposed recipe, warnings, and metadata
     * @throws RecipeExtractionException if the LLM provider is unavailable or the extraction fails entirely
     */
    ExtractionResult extractRecipeFromImage(byte[] imageBytes, String mimeType);
}
