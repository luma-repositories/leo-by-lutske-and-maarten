package be.lutske.leolegacy.interfaceadapter.rest

/**
 * Partial recipe DTO used during the import flow.
 * Fields are nullable because the LLM may not extract everything.
 */
data class ProposedRecipeDto(
    val title: String? = null,
    val description: String? = null,
    val servings: String? = null,
    val ingredients: List<String>? = null,
    val preparation: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null,
    val source: String? = null,
    val tags: List<String>? = null
)

/**
 * Response returned from POST /api/recipes/import after LLM extraction.
 * Always returned as HTTP 200 — the recipe is never auto-saved.
 *
 * Status values:
 * - "COMPLETE": all required fields were extracted successfully
 * - "NEEDS_MORE_INFO": some required fields are missing or uncertain
 * - "ERROR": extraction failed (e.g. image is not a recipe)
 */
data class ImportExtractionResponse(
    val status: String,
    val rawModelResponse: String? = null,
    val proposedRecipe: ProposedRecipeDto,
    val missingFields: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Request body for confirming/finalizing an imported recipe.
 */
data class ImportConfirmRequest(
    val rawModelResponse: String? = null,
    val proposedRecipe: ProposedRecipeDto,
    val userOverrides: UserOverrides? = null
)

/**
 * User-provided overrides and corrections for the imported recipe.
 */
data class UserOverrides(
    val title: String? = null,
    val ingredients: List<String>? = null,
    val preparation: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null,
    val servings: String? = null,
    val description: String? = null
)
