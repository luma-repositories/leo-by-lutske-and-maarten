package be.lutske.leolegacy.application.service

/**
 * Provider-agnostic interface for extracting structured recipe data from an image.
 *
 * Implementations use an LLM (via LangChain4j) to analyze the image and return
 * a structured extraction result. The provider (OpenAI, Claude, vLLM) is selected
 * via configuration — business logic never depends on a specific provider.
 */
interface RecipeExtractionService {

    /**
     * Extract recipe data from an image.
     *
     * @param imageBytes the raw image file bytes
     * @param mimeType the MIME type of the image (e.g. "image/png", "image/jpeg")
     * @return an [ExtractionResult] containing the proposed recipe, warnings, and metadata
     * @throws RecipeExtractionException if the LLM provider is unavailable or the extraction fails entirely
     */
    fun extractRecipeFromImage(imageBytes: ByteArray, mimeType: String): ExtractionResult
}

/**
 * Result of an LLM-based recipe extraction from an image.
 */
data class ExtractionResult(
    /** Extracted title, or null if the model could not determine it. */
    val title: String? = null,

    /** Extracted description / summary, or null. */
    val description: String? = null,

    /** Number of servings, or null if not found. */
    val servings: String? = null,

    /** Extracted ingredients as a list of strings. */
    val ingredients: List<String>? = null,

    /** Extracted preparation steps as a list of strings. */
    val steps: List<String>? = null,

    /** Source attribution if found in the image. */
    val source: String? = null,

    /** Tags / categories extracted from the recipe. */
    val tags: List<String>? = null,

    /** Warnings from the model about uncertain or missing data. */
    val warnings: List<String> = emptyList(),

    /** Sanitized raw model response for debugging / UI support. */
    val rawModelResponse: String? = null,

    /** The AI provider that was used (e.g. "openai", "claude", "vllm"). */
    val provider: String? = null,

    /** The model name that was used (e.g. "gpt-4o", "claude-sonnet-4-20250514"). */
    val model: String? = null
) {
    /**
     * Returns the list of required fields that are missing or empty.
     */
    fun missingFields(): List<String> {
        val missing = mutableListOf<String>()
        if (title.isNullOrBlank()) missing.add("title")
        if (ingredients.isNullOrEmpty()) missing.add("ingredients")
        if (steps.isNullOrEmpty()) missing.add("preparation")
        return missing
    }

    /**
     * Returns true if all required fields (title, ingredients, steps) are present.
     */
    fun isComplete(): Boolean = missingFields().isEmpty()
}

/**
 * Exception thrown when recipe extraction fails at the provider/model level.
 */
class RecipeExtractionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
